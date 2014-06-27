var expect = chai.expect
var testFrame = window.parent.frames[0]

function S(selector) {
    try {
        if (!testFrame.jQuery) {
            return $([])
        }
        return testFrame.jQuery(selector)
    } catch (e) {
        console.log("Premature access to testFrame.jQuery, printing stack trace.")
        console.log(new Error().stack);
        throw e;
    }
}

wait = {
    maxWaitMs: 10000,
    waitIntervalMs: 10,
    until: function(condition, count) {
        return function() {
            var deferred = Q.defer()
            if (count == undefined) count = wait.maxWaitMs / wait.waitIntervalMs;

            (function waitLoop(remaining) {
                if (condition()) {
                    deferred.resolve()
                } else if (remaining === 0) {
                    deferred.reject("timeout")
                } else {
                    setTimeout(function() {
                        waitLoop(remaining-1)
                    }, wait.waitIntervalMs)
                }
            })(count)
            return deferred.promise
        }
    }
}

session = {
    init: function(hetu) {
        return Q($.get("/omatsivut/util/fakesession?hetu=" + hetu));
    }
}

uiUtil = {
    inputValues: function(el) {
        return _.chain(el.find("[ng-model]"))
            .map(function(el) { return [$(el).attr("ng-model"), $(el).val() ]})
            .object().value()
    }
}

db = {
    resetData: function() {
        return Q($.ajax("/omatsivut/util/fixtures/apply", { type: "PUT" }))
    },

    getApplications: function() {
        return Q($.get("/omatsivut/api/applications"))
    },

    getPreferences: function() {
        return db.getApplications().then(function(data) {
            return _.chain(data[0].hakutoiveet)
                .filter(function(item) { return !_.isEmpty(item["Koulutus-id"]) })
                .map(function(item) {
                    return {
                        "hakutoive.Opetuspiste": item.Opetuspiste,
                        "hakutoive.Koulutus": item.Koulutus
                    }
                 }).value()
        })
    }
}

function openPage(path, predicate) {
    if (!predicate) {
        predicate = function() { return testFrame.jQuery }
    }
    return function() {
        testFrame.location.replace(path)
        return wait.until(predicate)()
    }
}