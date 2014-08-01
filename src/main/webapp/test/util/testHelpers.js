var expect = chai.expect
chai.should()
chai.config.truncateThreshold = 0; // disable truncating
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
  },
  untilFalse: function(condition) {
    return wait.until(function() { return !condition()})
  },
  forAngular: function() {
    var deferred = Q.defer()
    try {
      var angular = testFrame.angular
      var el = angular.element(S("#appRoot"))
      var timeout = angular.element(el).injector().get('$timeout')
      angular.element(el).injector().get('$browser').notifyWhenNoOutstandingRequests(function() {
        timeout(function() { deferred.resolve() })
      })
    } catch (e) {
      deferred.reject(e)
    }
    return deferred.promise
  },
  forMilliseconds: function(ms) {
    return function() {
      var deferred = Q.defer()
      setTimeout(function() {
        deferred.resolve()
      }, ms)
      return deferred.promise
    }
  }
}

session = {
  init: function(hetu,lang) {
    langParam = ""
    if (lang) {
      langParam = "&lang=" + lang
    }
    return Q($.get("/omatsivut/util/fakesession?hetu=" + hetu + langParam));
  }
}

uiUtil = {
  inputValues: function(el) {
    function formatKey(key) { return key.replace(".data.", ".") }
    function getId(el) { return [el.attr("ng-model"), el.attr("ng-bind")].join("") }

    return _.chain(el.find("[ng-model]:visible, [ng-bind]:visible"))
      .map(function(el) { return [formatKey(getId($(el))), $(el).val() + $(el).text() ]})
      .object().value()
  }
}

mockAjax = {
  init: function() {
    var deferred = Q.defer()
    if (testFrame.sinon)
      deferred.resolve()
    else
      testFrame.$.getScript('test/lib/sinon-server-1.10.3.js', function() { deferred.resolve() } )
    return deferred.promise
  },
  respondOnce: function (method, url, responseCode, responseBody) {
    var fakeAjax = function() {
      var xhr = sinon.useFakeXMLHttpRequest()
      xhr.useFilters = true
      xhr.addFilter(function(method, url) {
        return url != _fakeAjaxParams.url || method != _fakeAjaxParams.method
      })

      xhr.onCreate = function (request) {
        window.setTimeout(function() {
          if (window._fakeAjaxParams && request.method == _fakeAjaxParams.method && request.url == _fakeAjaxParams.url) {
            request.respond(_fakeAjaxParams.responseCode, { "Content-Type": "application/json" }, _fakeAjaxParams.responseBody)
            xhr.restore()
            delete _fakeAjaxParams
          }
        }, 0)
      }
    }

    testFrame._fakeAjaxParams = { method: method, url: url, responseCode: responseCode, responseBody: responseBody }
    testFrame.eval("(" + fakeAjax.toString() + ")()")
  }
}

db = {
  applyFixture: function(fixtureName) {
    return Q($.ajax("/omatsivut/util/fixtures/apply?fixturename=" + fixtureName, { type: "PUT" }))
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

function getJson(url) {
  return Q($.ajax({url: url, dataType: "json" }))
}

function openPage(path, predicate) {
  if (!predicate) {
    predicate = function() { return testFrame.jQuery }
  }
  return function() {
    S("body").attr("stale", "true")
    testFrame.location.replace(path)
    return wait.until(function() {
      return predicate() && !S("body").attr("stale")
    })()
  }
}

(function improveMocha() {
  var origBefore = before
  before = function() {
    Array.prototype.slice.call(arguments).forEach(function(arg) {
      if (typeof arg !== "function") {
        throw ("not a function: " + arg)
      }
      origBefore(arg)
    })
  }
})()