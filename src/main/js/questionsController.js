module.exports = function(listApp) {
  listApp.controller("questionsController", ["$scope", "$element", "$http", function ($scope, $element, $http) {
    $scope.$watch("application.getHakutoiveWatchCollection()", function(hakutoiveet, oldHakutoiveet) {
      // Skip initial values angular style
      var application = $scope.application

      var responsePromise = $http.post("api/applications/validate/" + application.oid, application.toJson());
      responsePromise.success(function(data, status, headers, config) {
        $scope.questions = data.questions
      })
    }, true)
  }])
}