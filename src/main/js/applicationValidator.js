var QuestionItem = require('./additionalQuestion')

function QuestionGroup(title) {
  this.title = title
  this.questionNodes = []
}

module.exports = function(listApp) {
  listApp.factory("applicationValidator", ["$http", function($http) {
    function getQuestions(data) {
      var errors = _.reduce(data.errors, function(memo, error) {
        if (memo[error.key] == null)
          memo[error.key] = []
        memo[error.key].push(error.message)
        return memo
      }, {})

      return convertToItems(data.questions, new QuestionGroup("päätaso")).questionNodes[0]

      function convertToItems(questions, results) {
        _(questions).each(function(questionNode) {
          if (questionNode.id) {
            results.questionNodes.push(new QuestionItem(questionNode, errors[questionNode.id.questionId] || []))
          } else {
            results.questionNodes.push(convertToItems(questionNode.questions, new QuestionGroup(questionNode.title )))
          }
        })
        return results
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