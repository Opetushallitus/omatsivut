require("angular/angular");
require('angular-resource/angular-resource');

var listApp = angular.module('listApp', ["ngResource"], function($locationProvider) {
    $locationProvider.html5Mode(true);
});

listApp.controller("listCtrl", ["$resource", "$scope", "$location", function ($resource, $scope, $location) {
    $scope.applications = $resource("api/applications/" + $location.search().hetu).query(function () {
    });
}]);