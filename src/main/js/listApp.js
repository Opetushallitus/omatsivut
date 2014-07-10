require("angular/angular");
require('angular-resource/angular-resource');
require('angular-animate/angular-animate');
_ = require("underscore");
require("../lib/ui-bootstrap-custom-tpls-0.10.0.min.js");

var listApp = angular.module('listApp', ["ngResource", "ngAnimate", "ui.bootstrap.typeahead", "template/typeahead/typeahead-popup.html", "template/typeahead/typeahead-match.html"], function($locationProvider) {
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
    $scope.applicationStatusMessage = "Hakemuksia ladataan...";
    $scope.applicationStatusMessageType = "ajax-spinner";
    applicationsResource.query(success, error)

    function success(data) {
        $scope.applications = data;
        $scope.applicationStatusMessage = "";
        $scope.applicationStatusMessageType = "";
    }

    function error(err) {
        switch (err.status) {
            case 401: $scope.applicationStatusMessage = "Tietojen lataus epäonnistui: et ole kirjautunut sisään."; break;
            default: $scope.applicationStatusMessage = "Tietojen lataus epäonnistui. Yritä myöhemmin uudelleen.";
        }
        $scope.applicationStatusMessageType = "error"
        $scope.applications = [];
    }
}]);

function hasData(item) {
    return _.any(item, function(val, key) { return key[0] != "$" })
}

listApp.controller("hakutoiveCtrl", ["$scope", "$http", function($scope, $http) {
    $scope.isNew = $scope.hakutoive["Opetuspiste-id"] === null

    $scope.$on("application-saved", function() {
        if (hasData($scope.hakutoive))
            $scope.isNew = false
    })

    $scope.oppilaitosValittu = function($item, $model, $label) {
        this.hakutoive["Opetuspiste"] = $item.name
        this.hakutoive["Opetuspiste-id"] = $item.id
        findKoulutukset(this.application.haku.oid, $item.id, this.application.educationBackground)
            .then(function(koulutukset) { $scope.koulutukset = koulutukset; })
    }

    $scope.koulutusValittu = function(index) {
        var koulutus = this["valittuKoulutus"]
        this.hakutoive["Koulutus"] = koulutus.name.toString()
        this.hakutoive["Koulutus-id"] = koulutus.id.toString()
        this.hakutoive["Koulutus-educationDegree"] = koulutus.educationDegree.toString()
        this.hakutoive["Koulutus-id-sora"] = koulutus.sora.toString()
        this.hakutoive["Koulutus-id-aoIdentifier"] = koulutus.aoIdentifier.toString()
        this.hakutoive["Koulutus-id-kaksoistutkinto"] = koulutus.kaksoistutkinto.toString()
        this.hakutoive["Koulutus-id-vocational"] = koulutus.vocational.toString()
        this.hakutoive["Koulutus-id-educationcode"] = koulutus.educationCodeUri.toString()
        this.hakutoive["Koulutus-id-athlete"] = koulutus.athleteEducation.toString()
        $scope.$parent.setDirty([$scope.index])
    }

    function findKoulutukset(applicationOid, opetuspisteId, educationBackground) {
        return $http.get("https://testi.opintopolku.fi/ao/search/" + applicationOid + "/" + opetuspisteId, {
            params: {
                baseEducation: educationBackground.baseEducation,
                vocational: educationBackground.vocational,
                uiLang: "fi" // TODO: kieliversio
            }
        }).then(function(res){
                return res.data;
            });
    }

    $scope.findOppilaitokset = function(val) {
        return $http.get('https://testi.opintopolku.fi/lop/search/' + val, {
            params: {
                asId: '1.2.246.562.5.2014022711042555034240'
            }
        }).then(function(res){
                return res.data;
            });
    };
}])

listApp.controller("hakemusCtrl", ["$scope", "$element", "$http", function ($scope, $element, $http) {
    $scope.hasChanged = false;
    $scope.isSaving = false;

    $scope.setDirty = function(indexes) {
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
        return index >= 0 && index <= this.application.hakutoiveet.length-1 && hasData(this.application.hakutoiveet[index]);
    }

    $scope.movePreference = function(from, to) {
        if (to >= 0 && to < this.application.hakutoiveet.length) {
            var arr = this.application.hakutoiveet;
            arr.splice(to, 0, arr.splice(from, 1)[0]);
            $scope.setDirty([from, to]);
            setSaveMessage();
        }
    };

    $scope.removePreference = function(index) {
        var row = this.application.hakutoiveet.splice(index, 1)[0];
        this.application.hakutoiveet.push({});
        $scope.hasChanged = true;
    };

    $scope.saveApplication = function() {
        $scope.isSaving = true;
        $scope.application.$update({id: $scope.application.oid }, onSuccess, onError);

        function onSuccess() {
            $scope.$emit("application-saved");
            $scope.$broadcast("application-saved");
            $scope.hasChanged = false;
            $scope.isSaving = false;
            setSaveMessage("Kaikki muutokset tallennettu", "success");
        }

        function onError(err) {
            switch (err.status) {
                case 401: setSaveMessage("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.", "error"); break
                default: setSaveMessage("Tallentaminen epäonnistui", "error")
            }

            $scope.isSaving = false
            console.log(err)
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
                    element.one("mouseout.cancelConfirm", cancel);
                    element.fadeIn(100)
                }
                return false;
            });
        }
    };
});