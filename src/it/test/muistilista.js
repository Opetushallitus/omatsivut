var assert = require("assert")
var Client = require('node-rest-client').Client;
var Q = require('q');
var Qretry = require('qretry');

describe('Muistilista QA', function () {
    this.timeout(6000);
    function httpPost(url, args) {
        return Q.Promise(function (resolve, reject) {
            var client = new Client();
            client.on('error', function (err) {
                reject(err);
            });
            client.post(url, args, function (data, response) {
                if (response.statusCode !== 200) {
                    reject(new Error("Status: " + response.statusCode + " for " + url));
                } else {
                    resolve(data, response);
                }
            });
        })
    }

    function httpGet(url) {
        return Q.Promise(function (resolve, reject) {
            var client = new Client();
            client.on('error', function (err) {
                reject(err);
            });
            client.get(url, function (data, response) {
                if (response.statusCode !== 200) {
                    reject(new Error("Status: " + response.statusCode + " for " + url));
                } else {
                    resolve(data, response);
                }
            });
        })
    }

    function parseEmails(data) {
        return data
            .replace(/(\r\n|\n|\r)/gm, "")
            .match(/href="(.*?)"/gm)
            .join(";")
            .replace(/(href=|")/gm, "")
            .split(";")
            .splice(1)
    }

    function parseLatestEmailUrl(emailDirectory) {
        return function (data) {
            var emails = parseEmails(data)
            return emailDirectory + emails[emails.length - 1];
        }
    }

    function assertContains() {
        console.log("POW!")
        var args = Array.prototype.slice.call(arguments)
        return function (fullText) {
            args.forEach(function (s) {
                if (fullText.indexOf(s) === -1) {
                    throw new Error("Assert failed: '" + s + "' was not included in: " + fullText);
                }
            })
            console.log("YES!")
        }
    }

    function retry(fn) {
        return function () {
            return Qretry(fn, {maxRetry: 10, interval: 100, intervalMultiplicator: 1})
        }
    }

    it('should send email to a file folder', function (done) {
        var emailDirectory = 'http://wp-reppu.oph.ware.fi/ryhmasahkoposti-emails/';
        var muistiListaEndPoint = "https://test-oppija.oph.ware.fi/omatsivut/muistilista";
        var muistilistaParams = {
            otsikko: "test subject " + Math.random(),
            kieli: "fi",
            vastaanottaja: ["foobar@example.com"],
            koids: ["1.2.246.562.14.2013092410023348364157"]
        }
        httpPost(muistiListaEndPoint, {
            data: muistilistaParams,
            headers: {"Content-Type": "application/json"}
        }).then(retry(function () {
                return httpGet(emailDirectory)
                    .then(parseLatestEmailUrl(emailDirectory))
                    .then(httpGet)
                    .then(assertContains(muistilistaParams.otsikko, muistilistaParams.vastaanottaja[0]))
            })
        ).then(done, done);

        // https://test-oppija.oph.ware.fi/omatsivut/muistilista -> http://wp-reppu.oph.ware.fi/ryhmasahkoposti-emails/
        // https://testi.opintopolku.fi/omatsivut/muistilista

    })
});
