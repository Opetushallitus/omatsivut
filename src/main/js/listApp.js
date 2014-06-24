require("angular/angular");
require('angular-resource/angular-resource');
_ = require("underscore");

var listApp = angular.module('listApp', ["ngResource"], function($locationProvider) {
    $locationProvider.html5Mode(true);
});

listApp.factory("applicationsResource", ["$resource", "$location", function($resource, $location) {
    return $resource("api/applications", null, {
        "update": {
            method: "PUT",
            url: "api/applications/:id"
        }
    });
}]);

listApp.factory("settings", [function() {
    var testMode = window.parent.location.href.indexOf("runner.html") > 0;
    return {
        uiTransitionTime: testMode ? 10 : 500
    };
}]);

listApp.controller("listCtrl", ["$scope", "applicationsResource", function ($scope, applicationsResource) {
    applicationsResource.query(success, error)

    function success(data) {
        $scope.applications = data;
    }

    function error() {
        $scope.errorText = "Tietojen lataus epäonnistui. Yritä myöhemmin uudelleen.";
        $scope.applications = [];
    }
}]);

listApp.controller("hakemusCtrl", ["$scope", "$element", function ($scope, $element) {
    $scope.changed = {};

    $scope.canMoveTo = function(start, end) {
        var self = this;
        function indexValid(index) {
            return index >= 0 && index <= self.application.hakutoiveet.length-1 && self.application.hakutoiveet[index]["Opetuspiste-id"] !== "";
        }
        return indexValid(start) && indexValid(end);
    };

    $scope.hasChanged = function() { return !_.isEmpty($scope.changed) };

    $scope.moveApplication = function(from, to) {
        if (to >= 0 && to < this.application.hakutoiveet.length) {
            var arr = this.application.hakutoiveet;
            arr.splice(to, 0, arr.splice(from, 1)[0]);
            $scope.changed[from] = true;
            $scope.changed[to] = true;
        }
    };

    $scope.saveApplication = function() {
        $scope.application.$update({id: $scope.application.oid }, onSuccess, onError);

        function onSuccess() {
            $scope.$emit("application-saved", _($scope.changed).chain().keys().map(Number).value());
            $scope.saveErrorMessage = "";
            $scope.changed = {};
        }

        function onError(err) {
            $scope.saveErrorMessage = "Tallentaminen epäonnistui";
            console.log(err);
        }
    };
}]);

listApp.directive('sortable', ["settings", function(settings) {
    return function($scope, $element) {
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
                moveDown(element1);
                moveUp(element2);
            } else {
                moveUp(element1);
                moveDown(element2);
            }

            setTimeout(function() {
                $scope.$apply(function() {
                    $scope.moveApplication(element1.index(), element2.index());
                    resetSlide(element1);
                    resetSlide(element2);
                });
            }, settings.uiTransitionTime);
        };

        var arrowClicked = function(elementF) {
            return function(evt) {
                var btn = $(evt.target);
                if (!btn.hasClass("disabled")) {
                    var element1 = btn.closest("li");
                    var element2 = element1[elementF]();
                    switchPlaces(element1, element2);
                }
            };
        };

        $element.on("click", ".sort-arrow-down", arrowClicked("next"));
        $element.on("click", ".sort-arrow-up", arrowClicked("prev"));
    };
}]);

listApp.directive("saveEffects", function() {
    return function($scope, $element) {
        $scope.$on("application-saved", function(evt, changedItems) {
            changedItems.forEach(function(index) {
                $element.children().eq(index).addClass("saved");
            })

            window.setTimeout(function() {
                $element.children().removeClass("saved");
            }, 3000);
        });
    };
});
