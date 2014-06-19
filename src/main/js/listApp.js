require("angular/angular");
require('angular-resource/angular-resource');
require('angular-animate/angular-animate');

var listApp = angular.module('listApp', ["ngResource", "ngAnimate"], function($locationProvider) {
    $locationProvider.html5Mode(true);
});


listApp.factory("applicationsResource", ["$resource", "$location", function($resource, $location) {
    return $resource("api/applications/" + $location.search().hetu, null, {
        "update": {
            method: "PUT",
            url: "api/applications/:id"
        }
    }).query(function () {
    });
}]);

listApp.controller("listCtrl", ["$scope", "applicationsResource", function ($scope, applicationsResource) {
    $scope.applications = applicationsResource;
}]);

listApp.controller("hakemusCtrl", ["$scope", function ($scope) {
    $scope.saved = true;

    $scope.moveApplication = function(from, to) {
        if (to >= 0 && to < this.application.hakutoiveet.length) {
            var arr = this.application.hakutoiveet;
            arr.splice(to, 0, arr.splice(from, 1)[0]);
        }
        $scope.saved = false;
    };

    $scope.saveApplication = function() {
        $scope.application.$update({id: $scope.application.oid }, onSuccess, onError);

        function onSuccess() {
            $scope.saved = true;
            $scope.saveErrorMessage = "";
        }

        function onError(err) {
            $scope.saveErrorMessage = "Tallentaminen epäonnistui";
            console.log(err);
        }
    };
}]);