require("angular/angular");
require('angular-resource/angular-resource');
require('angular-animate/angular-animate');

var listApp = angular.module('listApp', ["ngResource", "ngAnimate"], function($locationProvider) {
    $locationProvider.html5Mode(true);
});

listApp.controller("listCtrl", ["$resource", "$scope", "$location", function ($resource, $scope, $location) {
    $scope.applications = $resource("api/applications/" + $location.search().hetu).query(function () {
    });
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
        $scope.saved = true;
    };
}]);