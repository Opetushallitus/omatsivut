var QuestionItem = require('./additionalQuestion')

module.exports = function(listApp) {
  listApp.factory("applicationValidator", ["$http", function($http) {
    function getQuestions(data) {
      var errors = _.reduce(data.errors, function(memo, error) {
        if (memo[error.key] == null)
          memo[error.key] = []
        memo[error.key].push(error.message)
        return memo
      }, {})

      return flattenQuestions(data.questions)

      function flattenQuestions(questions) {
        return _.flatten(_(questions).map(function(questionNode) {
          if (questionNode.id) {
            return [new QuestionItem(questionNode, errors[questionNode.id.questionId] || [])]
          } else {
            return flattenQuestions(questionNode.questions)
          }
        }))
      }
    }


    return function(application, success) {
      var responsePromise = $http.post("api/applications/validate/" + application.oid, application.toJson())
      responsePromise.success(function(data, status, headers, config) {
        success(getQuestions(data))
      })
    }
  }])
}