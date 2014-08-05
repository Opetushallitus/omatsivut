var QuestionItem = require('./additionalQuestion').AdditionalQuestion
var QuestionGroup = require('./additionalQuestion').AdditionalQuestionGroup

module.exports = function(listApp) {
  listApp.factory("applicationValidator", ["$http", function($http) {

    return function applicationValidator() {
      var currentRequest

      function onlyIfCurrentRequest(current, f) {
        return function() {
          if (currentRequest === current)
            f.apply(this, arguments)
        }
      }

      return function(application, beforeBackendValidation, success, error) {
        currentRequest = {}
        success = onlyIfCurrentRequest(currentRequest, success)
        error = onlyIfCurrentRequest(currentRequest, error)

        var preferencesValid = application.validatePreferences()
        if (preferencesValid) {
          beforeBackendValidation()
          validateBackend(application, success, error)
        } else {
          error({
            errors: []
          })
        }
      }
    }

    function getQuestions(data) {
      return convertToItems(data.questions, new QuestionGroup())

      function convertToItems(questions, results) {
        _(questions).each(function (questionNode) {
          if (questionNode.id) {
            results.questionNodes.push(new QuestionItem(questionNode, questionNode.required ? ["*"] : []))
          } else {
            results.questionNodes.push(convertToItems(questionNode.questions, new QuestionGroup(questionNode.title)))
          }
        })
        return results
      }
    }

    function validateBackend(application, success, error) {
      var responsePromise = $http.post("/omatsivut/api/applications/validate/" + application.oid, application.toJson())
      responsePromise.success(function(data, status, headers, config) {
        if (data.errors.length === 0) {
          success({
            questions: getQuestions(data)
          })
        } else {
          error({
            statusCode: 200,
            errors: data.errors,
            questions: getQuestions(data)
          })
        }
      })

      responsePromise.error(function(data, status) {
        error({
          errors: [],
          statusCode: status,
          isSaveable: true
        })
      })
    }
  }])
}