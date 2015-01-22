var assert = require("assert")
require("./helpers.js")()

describe('Smoketest', function () {
    this.timeout(120000)

    var envs = {
        qa: {
            muistilista: "https://testi.opintopolku.fi/omatsivut/muistilista",
            "emailDirectory": "http://shibboleth2.qa.oph.ware.fi/ryhmasahkoposti-emails/"
        },
        systeemitesti: {
            muistilista: "https://test-oppija.oph.ware.fi/omatsivut/muistilista",
            emailDirectory: "http://wp-reppu.oph.ware.fi/ryhmasahkoposti-emails/"
        }
    }

    describe("Systeemitesti (reppu)", function() {
        it("/omatsivut/muistilista email", muistilistaEmailTest(envs.systeemitesti))
    })

    describe("QA (reppu)", function() {
        it("/omatsivut/muistilista email", muistilistaEmailTest(envs.qa))
    })

    function parseEmails(data) {
        return data
            .replace(/(\r\n|\n|\r)/gm, "")
            .match(/href="(.*?)"/gm)
            .join(";")
            .replace(/(href=|")/gm, "")
            .split(";")
    }

    function parseLatestEmailUrl(emailDirectory) {
        return function (data) {
            var emails = parseEmails(data)
            return emailDirectory + emails[emails.length - 1]
        }
    }

    function muistilistaEmailTest(env) {
        return function (done) {
            var muistilistaParams = {
                otsikko: "test subject " + Math.random(),
                kieli: "fi",
                vastaanottaja: ["foobar@example.com"],
                koids: ["1.2.246.562.14.2013092410023348364157"]
            }
            httpPost(env.muistilista, {
                data: muistilistaParams,
                headers: {"Content-Type": "application/json"}
            }).then(retry(function () {
                    return httpGet(env.emailDirectory)
                        .then(parseLatestEmailUrl(env.emailDirectory))
                        .then(httpGet)
                        .then(assertContains(muistilistaParams.otsikko, muistilistaParams.vastaanottaja[0]))
                })
            ).then(done, done)
        }
    }
})
