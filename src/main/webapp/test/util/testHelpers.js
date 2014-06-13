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
    until: function(condition, count) {
        return function(callback) {
            if (count == undefined) count = 100
            if (condition()) {
                callback()
            } else {
                setTimeout(function() {
                    wait.until(condition, callback, count - 1)(callback)
                }, 10)
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