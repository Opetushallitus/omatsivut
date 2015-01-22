var Q = require('q')
var Qretry = require('qretry')
var Client = require('node-rest-client').Client

module.exports = function () {

    this.assertContains = function () {
        var args = Array.prototype.slice.call(arguments)
        return function (fullText) {
            args.forEach(function (s) {
                if (fullText.indexOf(s) === -1) {
                    throw new Error("Assert failed: '" + s + "' was not included in: " + fullText)
                }
            })
        }
    }

    this.retry = function (fn) {
        return function () {
            return Qretry(fn, {maxRetry: 10, interval: 100, intervalMultiplicator: 1})
        }
    }

    this.httpPost = function (url, args) {
        return httpOperation("post", [url, args])
    }

    this.httpGet = function httpGet(url) {
        return httpOperation("get", [url])
    }

    function httpOperation(method, args) {
        return Q.Promise(function (resolve, reject) {
            var client = new Client()
            client.on('error', function (err) {
                reject(new Error("Error for url " + args[0] + " :"))
            })
            args.push(function (data, response) {
                if (response.statusCode !== 200) {
                    reject(new Error("Status: " + response.statusCode + " for " + args[0]))
                } else {
                    resolve(data, response)
                }
            })
            client[method].apply(this, args)
        })
    }
}