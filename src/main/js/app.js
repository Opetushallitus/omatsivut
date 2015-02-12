require("angular");
require('ng-resource')(window, angular);
require('angular-module-sanitize');
require('angular-animate');
_ = require("underscore");
require("../lib/ui-bootstrap-custom-tpls-0.10.0.min.js");
require('./recursionHelper')
require('../lib/angular-debounce')

window.moment = require("moment");
require("../lib/moment-locale-fi.js");
require("moment/locale/sv.js");
require("moment/locale/en-gb.js");

angular.module("templates", [])
require("../webapp/hakemuseditori-templates.js")
require("../templates/templates.js")

var listApp = angular.module('listApp', ["ngResource", "ngSanitize", "ngAnimate", "RecursionHelper", "ui.bootstrap.typeahead", "template/typeahead/typeahead-popup.html", "template/typeahead/typeahead-match.html", "debounce", "exceptionOverride", "templates"], function($locationProvider) {
  $locationProvider.html5Mode(false)
});

var staticResources = require('./staticResources')

require('./restResources')(listApp)

require('./hakemuseditori')(listApp)

require('./settings')(listApp, testMode())

require('./directives/applicationList')(listApp)

listApp.run(function ($rootScope, localization) {
  $rootScope.localization = localization
})

var raamitLoaded = $.Deferred()
if (document.location.hash.indexOf("skipRaamit") > 0 || $("#siteheader").length > 0) {
  raamitLoaded.resolve()
}
$("html").on("oppija-raamit-loaded", function() {
  raamitLoaded.resolve()
})

angular.element(document).ready(function() {
  staticResources.init(function() {
    raamitLoaded.done(function() {
      require('./localization')(listApp, staticResources)
      angular.bootstrap(document, ['listApp'])
      $("body").attr("aria-busy","false")
    })
  })
})

function testMode() {
  return window.parent.location.href.indexOf("runner.html") > 0
}

function logExceptionToPiwik(msg, data) {
  if (typeof _paq === 'undefined' || _paq == null) {
    console.warn("Piwik not present, cannot log: " + msg + "\n" + data)
  } else {
    console.warn(msg + "\n" + data)
    _paq.push(["trackEvent", document.location, msg, data])
  }
}

window.onerror = function(errorMsg, url, lineNumber, columnNumber, exception) {
  var data = url + ":" + lineNumber
  if (typeof columnNumber !== "undefined") data += ":" + columnNumber
  if (typeof exception !==  "undefined") data += "\n" + exception.stack
  logExceptionToPiwik(errorMsg, data)
}

angular.module("exceptionOverride", []).factory("$exceptionHandler", function() {
  return function (exception) {
    if (testMode()) {
      throw exception
    } else {
      logExceptionToPiwik(exception.message, exception.stack)
    }
  };
})
