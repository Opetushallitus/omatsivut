var QuestionItem = require('./additionalQuestion')

module.exports = function(listApp) {
  listApp.factory("applicationValidator", ["$http", function($http) {
    function getQuestions(data) {
      var errors = _.reduce(data.errors, function(memo, error) {
        if (memo[error.key] == null)
          memo[error.key] = []
        memo[error.key].push(error.translation.translations["fi"])
        return memo
      }, {})

      return _(data.questions).map(function(question) {
        return new QuestionItem(question, errors[question.id.questionId] || [])
      })
    }

    return function(application, success) {
      var responsePromise = $http.post("api/applications/validate/" + application.oid, application.toJson())
      responsePromise.success(function(data, status, headers, config) {
        success(getQuestions(data))
      })
    }
  }])
}