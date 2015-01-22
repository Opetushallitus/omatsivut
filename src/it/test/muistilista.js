var assert = require("assert")
var Client = require('node-rest-client').Client
var Q = require('q')
var Qretry = require('qretry')

describe('Muistilista QA', function () {
    this.timeout(6000)
    function httpPost(url, args) {
        return httpOperation("post", [url, args])
    }

    function httpGet(url) {
        return httpOperation("get", [url])
    }

    function httpOperation(method, args) {
        return Q.Promise(function (resolve, reject) {
            var client = new Client()
            client.on('error', function (err) {
                reject(err)
            })
            args.push(function (data, response) {
                if (response.statusCode !== 200) {
                    reject(new Error("Status: " + response.statusCode + " for " + url))
                } else {
                    resolve(data, response)
                }
            })
            client[method].apply(this, args)
        })
    }
    
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

    function assertContains() {
        var args = Array.prototype.slice.call(arguments)
        return function (fullText) {
            args.forEach(function (s) {
                if (fullText.indexOf(s) === -1) {
                    throw new Error("Assert failed: '" + s + "' was not included in: " + fullText)
                }
            })
        }
    }

    function retry(fn) {
        return function () {
            return Qretry(fn, {maxRetry: 10, interval: 100, intervalMultiplicator: 1})
        }
    }

    // Systeemitesti https://test-oppija.oph.ware.fi/omatsivut/muistilista -> http://wp-reppu.oph.ware.fi/ryhmasahkoposti-emails/
    // QA https://testi.opintopolku.fi/omatsivut/muistilista -> http://shibboleth2.qa.oph.ware.fi/ryhmasahkoposti-emails/

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

    it('should send email to a file folder', function (done) {
        var env = envs.qa
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

    })
})
