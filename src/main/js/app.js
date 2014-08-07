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

angular.module("exceptionOverride", []).factory("$exceptionHandler", function () {
  return function (exception) {
    throw exception
  };
})

listApp.factory("settings", ["$animate", function($animate) {
  var testMode = window.parent.location.href.indexOf("runner.html") > 0;
  if (testMode) $animate.enabled(false);

  return {
    uiTransitionTime: testMode ? 10 : 500,
    modelDebounce: testMode? 0 : 300
  };
}]);

listApp.directive('sortable', ["settings", function(settings) {
  return function($scope, $element, attrs) {
    var slide = function(el, offset) {
      el.css("transition", "all 0.5s");
      el.css("transform", "translate3d(0px, " + offset + "px, 0px)");
    };

    var moveDown = function(el) {
      slide(el, el.outerHeight());
    };

    var moveUp = function(el) {
      slide(el, -el.outerHeight());
    };

    var resetSlide = function(el) {
      el.css({
        "transition": "",
        "transform": ""
      });
    };

    var switchPlaces = function(element1, element2) {
      if (element1.index() < element2.index()) {
        moveDown(element1)
        moveUp(element2)
      } else {
        moveUp(element1)
        moveDown(element2)
      }

      setTimeout(function() {
        $scope.$apply(function(self) {
          var items = $element.find(attrs.sortableItem)
          self[attrs.sortableMoved](items.index(element1), items.index(element2))
          resetSlide(element1)
          resetSlide(element2)
        });
      }, settings.uiTransitionTime)
    };

    var arrowClicked = function(elementF) {
      return function(evt) {
        var btn = $(evt.target);
        if (!btn.hasClass("disabled")) {
          var element1 = btn.closest(attrs.sortableItem);
          var element2 = element1[elementF]();
          switchPlaces(element1, element2);
        }
      };
    };

    $element.on("click", ".sort-arrow-down", arrowClicked("next"));
    $element.on("click", ".sort-arrow-up", arrowClicked("prev"));
  };
}]);

listApp.directive("highlightSave", function() {
  return function($scope, $element) {
    $scope.$on("highlight-save", function(event, indexes) {
      var items = $element.find(".preference-list-item")

      _.each(indexes, function(index) {
        items.eq(index).addClass("saved")
      })

      $element.find(".timestamp").addClass("saved")

      window.setTimeout(function() {
        items.removeClass("saved")
        $(".timestamp").removeClass("saved")
      }, 3000);
    });
  };
});

listApp.directive("disableClickFocus", function() {
  return {
    link: function (scope, element) {
      element.on("mousedown", function(event) {
        event.preventDefault()
      })
    }
  }
})

listApp.directive("confirm", function () {
  return {
    scope: {
      callback : '&confirmAction'
    },
    link: function (scope, element, attrs) {
      function cancel() {
        element.removeClass("confirm");
        element.text(originalText);
        element.off(".cancelConfirm");
        $("body").off(".cancelConfirm");
      }

      var originalText = element.text();

      element.on("click", function() {
        if (element.hasClass("confirm")) {
          scope.$apply(scope.callback);
          cancel();
        } else {
          element.hide()
          element.addClass("confirm");
          element.text(attrs.confirmText);
          $("body").one("click.cancelConfirm", cancel);
          element.one("mouseout.cancelConfirm", function() { element.blur() });
          element.one("blur.cancelConfirm", cancel);
          element.fadeIn(100)
        }
        return false;
      });
    }
  };
});

listApp.directive("questionTemplate", function(RecursionHelper) {
  return {
    restrict: 'E',
    scope: {
      questionNode: '=questionNode',
      application: '=application',
      level: '=level'
    },
    templateUrl: 'questionTemplate.html',
    compile: function(element) {
      return RecursionHelper.compile(element, function($scope, iElement, iAttrs, controller, transcludeFn){
        $scope.isGroup = function() {
          return $scope.questionNode && !_.isEmpty($scope.questionNode.questionNodes)
        }
      });
    }
  };
});

