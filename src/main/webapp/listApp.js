var listApp = angular.module('listApp', ["ngResource"]);

listApp.controller("listCtrl", ["$resource", "$scope", function ($resource, $scope) {
    $scope.applications = $resource("/api/applications").query(function () {
    });
}]);