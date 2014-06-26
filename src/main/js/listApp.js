require("angular/angular");
require('angular-resource/angular-resource');
require('angular-animate/angular-animate');
_ = require("underscore");


var listApp = angular.module('listApp', ["ngResource", "ngAnimate"], function($locationProvider) {
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

listApp.factory("settings", ["$animate", function($animate) {
    var testMode = window.parent.location.href.indexOf("runner.html") > 0;
    if (testMode) $animate.enabled(false);

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
    $scope.hasChanged = false;

    function setDirty(indexes) {
        $scope.$emit("application-preferences-changed", indexes);
        $scope.hasChanged = true;
    }

    function setSaveMessage(msg, type) {
        $scope.saveMessage = msg;
        $scope.saveMessageType = type;
    }

    $scope.$watch("hasChanged", function(val) {
        if (val===true) setSaveMessage("");
    });

    $scope.canMoveTo = function(from, to) {
        return $scope.hasPreference(from) && $scope.hasPreference(to);
    };

    $scope.hasPreference = function(index) {
        return index >= 0 && index <= this.application.hakutoiveet.length-1 && this.application.hakutoiveet[index]["Opetuspiste-id"] !== "";
    }

    $scope.movePreference = function(from, to) {
        if (to >= 0 && to < this.application.hakutoiveet.length) {
            var arr = this.application.hakutoiveet;
            arr.splice(to, 0, arr.splice(from, 1)[0]);
            setDirty([from, to]);
            setSaveMessage();
        }
    };

    $scope.removePreference = function(index) {
        var row = this.application.hakutoiveet.splice(index, 1)[0];
        var newRow = {};
        _.each(row, function(val, key) { if (key[0] != "$") newRow[key] = "" });
        this.application.hakutoiveet.push(newRow);
        $scope.hasChanged = true;
    };

    $scope.saveApplication = function() {
        $scope.application.$update({id: $scope.application.oid }, onSuccess, onError);

        function onSuccess() {
            $scope.$emit("application-saved");
            $scope.hasChanged = false;
            setSaveMessage("Kaikki muutokset tallennettu", "success");
        }

        function onError(err) {
            setSaveMessage("Tallentaminen epäonnistui", "error");
            console.log(err);
        }
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
                moveDown(element1);
                moveUp(element2);
            } else {
                moveUp(element1);
                moveDown(element2);
            }

            setTimeout(function() {
                $scope.$apply(function(self) {
                    self[attrs.sortableMoved](element1.index(), element2.index());
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
        $scope.$on("application-preferences-changed", function(evt, changedItems) {
            _.each(changedItems, function(index) {
                $element.find(".preference-list-item").eq(index).addClass("preference-changed");
            });
        });

        $scope.$on("application-saved", function(evt) {
            $element.find(".preference-list-item.preference-changed")
                .removeClass("preference-changed")
                .addClass("saved")

            window.setTimeout(function() {
                $element.find(".preference-list-item").removeClass("saved");
            }, 3000);
        });
    };
});

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
            };

            var originalText = element.text();

            element.on("click", function() {
                if (element.hasClass("confirm")) {
                    scope.$apply(scope.callback);
                    cancel();
                } else {
                    element.addClass("confirm");
                    element.text(attrs.confirmText);
                    $("body").one("click.cancelConfirm", cancel);
                    element.one("mouseout.cancelConfirm", cancel);
                }
                return false;
            });
        }
    };
});