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
require("../templates/templates.js")

var listApp = angular.module('listApp', ["ngResource", "ngSanitize", "ngAnimate", "RecursionHelper", "ui.bootstrap.typeahead", "template/typeahead/typeahead-popup.html", "template/typeahead/typeahead-match.html", "debounce", "exceptionOverride", "templates"], function($locationProvider) {
  $locationProvider.html5Mode(false)
});

var staticResources = require('./staticResources')
require('./localization')(listApp, staticResources)
require('./restResources')(listApp)

require('./applicationValidator')(listApp)
require('./settings')(listApp, testMode())
require('./angularBacon')(listApp)

require('./directives/callout')(listApp)
require('./directives/confirm')(listApp)
require('./directives/localizedLink')(listApp)
require('./directives/formattedTime')(listApp)
require('./directives/sortable')(listApp)
require('./directives/disableClickFocus')(listApp)
require('./directives/question')(listApp)
require('./directives/applicationList')(listApp)
require('./directives/application')(listApp)
require('./directives/hakutoiveenVastaanotto')(listApp)
require('./directives/hakutoiveet')(listApp)
require('./directives/valintatulos')(listApp)
require('./directives/henkilotiedot')(listApp)
require('./directives/applicationPeriods')(listApp)
require('./directives/clearableInput')(listApp)

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
      angular.bootstrap(document, ['listApp'])
      $("body").attr("aria-busy","false")
    })
  })
})

function testMode() {
  return window.parent.location.href.indexOf("runner.html") > 0
}

function logExceptionToPiwik(msg) {
  if (_paq) {
    _paq.push(["trackEvent", document.domain + "/" + document.title, "Error", msg])
  } else {
    console.warning("Piwik not present")
  }
}

window.onerror = function(errorMsg, url, lineNumber) {
  logExceptionToPiwik(url + ":" + lineNumber + " " + errorMsg)
}

angular.module("exceptionOverride", []).factory("$exceptionHandler", function() {
  return function (exception) {
    if (testMode()) {
      throw exception
    } else {
      logExceptionToPiwik(exception.stack || exception.message)
      console.error(exception.stack)
    }
  };
})
