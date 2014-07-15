module.exports = function(listApp) {
  listApp.controller("questionsController", ["$scope", "$element", "$http", function ($scope, $element, $http) {
    $scope.$watch("application.getHakutoiveWatchCollection()", function(hakutoiveet, oldHakutoiveet) {
      // Skip initial values angular style
      var application = $scope.application

      var responsePromise = $http.post("api/applications/validate/" + application.oid, application.toJson());
      responsePromise.success(function(data, status, headers, config) {
        $scope.questions = data.questions
        $scope.questions.forEach(function(question) {
          question.answer = $scope.application.getAnswer(question)
        })
      })
    }, true)
  }])

  listApp.controller("questionController", ["$scope", "$element", function ($scope, $element) {
    $scope.$watch("question.answer", function(answer, prevAnswer) {
      if (answer !== prevAnswer) {
        var question = $scope.question
        $scope.application.setAnswer(question, answer)
        $scope.$emit("applicationChange")
      }
    })
  }])
}