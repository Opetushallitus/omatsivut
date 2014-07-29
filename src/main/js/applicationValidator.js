var QuestionItem = require('./additionalQuestion').AdditionalQuestion
var QuestionGroup = require('./additionalQuestion').AdditionalQuestionGroup
var util = require('./util')
var domainUtil = require('./domainUtil')

module.exports = function(listApp) {
   listApp.factory("applicationValidator", ["$http", function($http) {
    function getQuestions(data) {
      return convertToItems(data.questions, new QuestionGroup())

      function convertToItems(questions, results) {
        _(questions).each(function(questionNode) {
          if (questionNode.id) {
            results.questionNodes.push(new QuestionItem(questionNode, questionNode.required ? ["*"] : []))
          } else {
            results.questionNodes.push(convertToItems(questionNode.questions, new QuestionGroup(questionNode.title )))
          }
        })
        return results
      }
    }

    return function() {
      var currentRequest

      function onlyIfCurrentRequest(current, f) {
        return function() {
          if (currentRequest === current)
            f.apply(this, arguments)
        }
      }

      return function(application, success, error) {
        currentRequest = {}
        success = onlyIfCurrentRequest(currentRequest, success)
        error = onlyIfCurrentRequest(currentRequest, error)

        var preferencesValid = application.validatePreferences()
        if (preferencesValid) {
          validateBackend(application, success, error)
        } else {
          error({
            isSaveable: false
          })
        }
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
            isSaveable: !hasHakutoiveErrors(data.errors),
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

    function hasHakutoiveErrors(errors) {
      var errorMap = util.mapArray(errors, "key", "message");
      return _(errorMap).any(function(val, key) {
        return domainUtil.isHakutoiveError(key) && val.length > 0
      })
    }
  }])
}