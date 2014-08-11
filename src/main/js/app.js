require("angular");
require('ng-resource')(window, angular);
require('angular-animate');
_ = require("underscore");
require("../lib/ui-bootstrap-custom-tpls-0.10.0.min.js");
window.moment = require("moment");
require("moment/locale/fi.js");
require("moment/locale/sv.js");
require("moment/locale/en-gb.js");

var listApp = angular.module('listApp', ["ngResource", "ngAnimate", "RecursionHelper", "ui.bootstrap.typeahead", "template/typeahead/typeahead-popup.html", "template/typeahead/typeahead-match.html", "debounce", "exceptionOverride"], function($locationProvider) {
  $locationProvider.html5Mode(false);
});

listApp.run(function ($rootScope, localization) {
  $rootScope.localization = localization
})

var staticResources = require('./staticResources')
require('./hakutoiveController')(listApp)
require('./listController')(listApp)
require('./hakemusController')(listApp)
require('./applicationValidator')(listApp)
require('./localization')(listApp, staticResources)
require('./directives')(listApp)
require('./recursionHelper')
require('../lib/angular-debounce')

var raamitLoaded = $.Deferred()
if (document.location.hash.indexOf("skipRaamit") > 0) {
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

listApp.factory("applicationsResource", ["$resource", "$location", function($resource, $location) {
  return $resource("/omatsivut/api/applications", null, {
    "update": {
      method: "PUT",
      url: "/omatsivut/api/applications/:id"
    }
  });
}]);

function testMode() {
  return window.parent.location.href.indexOf("runner.html") > 0
}

angular.module("exceptionOverride", []).factory("$exceptionHandler", function() {
  return function (exception) {
    if (testMode())
      throw exception
    else
      console.error(exception.stack)
  };
})

listApp.factory("settings", ["$animate", function($animate) {
  if (testMode()) $animate.enabled(false)
  return {
    uiTransitionTime: testMode() ? 10 : 500,
    modelDebounce: testMode() ? 0 : 300,
    uiIndicatorDebounce: testMode() ? 0: 500
  };
}]);

