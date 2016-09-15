var expect = chai.expect
chai.should()
chai.config.truncateThreshold = 0; // disable truncating

function S(selector) {
  try {
    if (!testFrame() || !testFrame().jQuery) {
      return $([])
    }
    return testFrame().jQuery(selector)
  } catch (e) {
    console.log("Premature access to testFrame.jQuery, printing stack trace.")
    console.log(new Error().stack);
    throw e;
  }
}

wait = {
  waitIntervalMs: 10,
  until: function(condition, maxWaitMs) {
    return function() {
      if (maxWaitMs == undefined) maxWaitMs = testTimeoutDefault;
      var deferred = Q.defer()
      var count = maxWaitMs / wait.waitIntervalMs;

      (function waitLoop(remaining) {
        if (condition()) {
          deferred.resolve()
        } else if (remaining === 0) {
          deferred.reject("timeout of " + maxWaitMs + " in wait.until of condition:\n" + condition)
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
      var angular = testFrame().angular
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
    return function () {
      langParam = ""
      if (lang) {
        langParam = "&lang=" + lang
      }
      return Q($.get("/omatsivut/Shibboleth.sso/fakesession?hetu=" + hetu + langParam));
    }
  },
  logout: function() {
    return Q($.get("/omatsivut/logout"));
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
    if (testFrame().sinon)
      deferred.resolve()
    else
      testFrame().$.getScript('test/lib/sinon-server-1.10.3.js', function() { deferred.resolve() } )
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

    testFrame()._fakeAjaxParams = { method: method, url: url, responseCode: responseCode, responseBody: responseBody }
    testFrame().eval("(" + fakeAjax.toString() + ")()")
  }
}

util = {
  flattenObject: function(obj) {
    function flatten(obj, prefix, result) {
      _.each(obj, function(val, id) {
        if (_.isObject(val)) {
          flatten(val, id + ".", result)
        } else {
          result[prefix + id] = val
        }
      })
      return result
    }
    return flatten(obj, "", {})
  }
}

db = {
  getApplications: function() {
    return Q($.get("/omatsivut/secure/applications"))
  },

  getPreferences: function() {
    return db.getApplications().then(function(data) {
      return _.chain(data[0].hakemus.hakutoiveet)
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

fixtures = {
  applyFixture: function(fixtureName, applicationOid) {
    applicationOid = applicationOid || "*"
    return Q($.ajax({url: "/omatsivut/util/fixtures/hakemus/apply?fixturename=" + fixtureName + "&applicationOid=" + applicationOid, type: "PUT"}))
  },

  applyErillishaku: function(hyvaksytty) {
    return Q($.ajax({url: "/omatsivut/util/fixtures/erillishaku?hyvaksytty=" + hyvaksytty, type: "PUT"}))
  },

  applyValintatulos: function(fixtureName, otherFixtures) {
    var query = ""
    if(otherFixtures) {
      if(otherFixtures.ohjausparametrit) {
        query = "&ohjausparametrit=" + otherFixtures.ohjausparametrit
      }
      if(otherFixtures.haku) {
        query = query + "&haku=" + otherFixtures.haku
      }
      if(otherFixtures.hakuoid) {
        query = query + "&useHakuOid=" + otherFixtures.hakuoid
      }
    }
    return Q($.ajax({url: "/omatsivut/util/fixtures/valintatulos/apply?fixturename=" + fixtureName + query, type: "PUT"}))
  },

  setApplicationStart: function(applicationId, startTime) {
    var f = function(hakuOid) {
      return Q($.ajax({url: "/omatsivut/util/fixtures/haku/" + hakuOid + "/overrideStart/" + startTime, type: "PUT"}))
    }
    return this.setHakuData(applicationId, f)
  },

  resetApplicationStart: function(applicationId) {
    var f = function(hakuOid) {
      return Q($.ajax({url: "/omatsivut/util/fixtures/haku/" + hakuOid + "/resetStart", type: "PUT"}))
    }
    return this.setHakuData(applicationId, f)
  },

  setInvertedPriority: function(applicationId) {
    var f = function (hakuOid) {
      return Q($.ajax({url: "/omatsivut/util/fixtures/haku/" + hakuOid + "/invertPriority", type: "PUT"}))
    }
    return this.setHakuData(applicationId, f)
  },

  setHakuData: function(applicationId, f) {
    return db.getApplications().then(function(applications) {
      var hakuOid = _(applications).find(function(application) { return application.hakemus.oid === applicationId }).hakemus.haku.oid
      return f(hakuOid)
    })
  },

  setValintatulosServiceFailure: function(fail) {
    return Q($.ajax({url: "/omatsivut/util/fixtures/valintatulos/fail/" + fail, type: "PUT"}))
  }
}

function getJson(url) {
  return Q($.ajax({url: url, dataType: "json" }))
}

function testFrame() {
  return $("#testframe").get(0).contentWindow
}

function openPage(path, predicate) {
  if (!predicate) {
    predicate = function() { return testFrame().jQuery }
  }
  return function() {
    var newTestFrame = $('<iframe>').attr({src: path, width: 1024, height: 800, id: "testframe"})
    $("#testframe").replaceWith(newTestFrame)
    return wait.until(
        function() {
          return predicate()
        },
        testTimeoutPageLoad
    )().then(function() {
        window.uiError = null
        testFrame().onerror = function(err) { window.uiError = err; } // Hack: force mocha to fail on unhandled exceptions
    })
  }
}

function takeScreenshot() {
  if (window.callPhantom) {
    var date = new Date()
    var filename = "target/screenshots/" + date.getTime()
    console.log("Taking screenshot " + filename)
    callPhantom({'screenshot': filename})
  }
}

function compareDOMIndex(el1, el2) {
  function indexInTree(element, indexes) {
    indexes = indexes || []
    indexes.unshift(element.index())
    if (element.parent().length)
      indexInTree(element.parent(), indexes)
    return indexes
  }

  function compareTrees(tree1, tree2) {
    for (var i=0; i<tree1.length && i < tree2.length; i++) {
      if (tree1[i] < tree2[i])
        return -1
      else if (tree1[i] > tree2[i])
        return 1
    }
    return 0
  }

  return compareTrees(indexInTree(el1), indexInTree(el2))
}

function daysFromNow(days) {
  return new Date().getTime()+days*24*60*60*1000
}

function removeSpaces(text) {
  return text.replace(/(\r\n|\n|\r)/gm,"").replace(/\s+/g," ").trim()
}

function trimText() {
  return removeSpaces($(this).text())
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