var QuestionItem = require('./additionalQuestion').AdditionalQuestion
var QuestionGroup = require('./additionalQuestion').AdditionalQuestionGroup
var util = require('./util')

module.exports = function(listApp) {
  listApp.factory("applicationValidator", ["$http", function($http) {
    function getQuestions(data) {
      var errorMap = util.mapArray(data.errors, "key", "message")
      return convertToItems(data.questions, new QuestionGroup())

      function convertToItems(questions, results) {
        _(questions).each(function(questionNode) {
          if (questionNode.id) {
            results.questionNodes.push(new QuestionItem(questionNode, errorMap[questionNode.id.questionId] || []))
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