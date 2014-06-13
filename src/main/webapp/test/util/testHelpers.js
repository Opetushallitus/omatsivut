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
        return function(callback) {
            if (count == undefined) count = wait.maxWaitMs / wait.waitIntervalMs
            if (condition()) {
                callback()
            } else {
                setTimeout(function() {
                    wait.until(condition, callback, count - 1)(callback)
                }, wait.waitIntervalMs)
            }
        }
    }
}
function openPage(path, predicate) {
    if (!predicate) {
        predicate = function() { return testFrame.jQuery }
    }
    return function(done) {
        testFrame.location.replace(path)
        wait.until(predicate)(done)
    }
}