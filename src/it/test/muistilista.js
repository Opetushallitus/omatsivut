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

    function assertContains(txt, fullText) {
        if (fullText.indexOf(txt) === -1) {
            throw new Error("Assert failed: " + txt + " was not included in: " + fullText);
        }
    }

    function retry(attempts, fn, resolve, reject) {
        if (resolve === undefined) {
            console.log("retry start")
            return Q.Promise(function(resolve, reject){
                retry(attempts, fn, resolve, reject);
            });
        } else {
            console.log("retry " + attempts)
            try {
                resolve(fn())
            } catch(error) {
                if(attempts == 0) {
                    reject(error)
                } else {
                    var callback = new function(){retry(resolve, reject, attempts - 1, fn)};
                    setTimeout(callback, 50)
                }
            }
        }
    }

    it('should send email to a file folder', function (done) {
        var emailDirectory = 'http://wp-reppu.oph.ware.fi/ryhmasahkoposti-emails/';
        var muistiListaEndPoint = "https://test-oppija.oph.ware.fi/omatsivut/muistilista";
        var randomSubject = "test subject " + Math.random()
        httpPost(muistiListaEndPoint, {
            data: {
                "otsikko": randomSubject,
                "kieli": "fi",
                "vastaanottaja": ["foobar@example.com"],
                "koids": ["1.2.246.562.14.2013092410023348364157"]
            },
            headers: {"Content-Type": "application/json"}
        }).then(function (data, response) {
            return Qretry(
                function() {
                    console.log("POW!")
                    return httpGet(emailDirectory).then(function (data, response) {
                        var emails = parseEmails(data);
                        var latestEmailUrl = emailDirectory + emails[emails.length - 1];
                        return httpGet(latestEmailUrl)
                    }).then(function (data, response) {
                        assertContains(randomSubject, data)
                        console.log("YES!")
                    })
                },{ maxRetry: 20, interval: 100 }
            )
        }).then(
            function() {
                done();
            },
            function (reason) {
                done(reason);
            });

        // https://test-oppija.oph.ware.fi/omatsivut/muistilista -> http://wp-reppu.oph.ware.fi/ryhmasahkoposti-emails/
        // https://testi.opintopolku.fi/omatsivut/muistilista

    })
});
