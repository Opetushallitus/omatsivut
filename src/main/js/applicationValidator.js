var Question = require('./question').Question
var QuestionGroup = require('./question').QuestionGroup

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

    function getQuestions(data, application) {
      return convertToItems(data.questions, new QuestionGroup())

      function convertToItems(questions, results) {
        _(questions).each(function (questionNode) {
          if (questionNode.questions != null) {
            results.questionNodes.push(convertToItems(questionNode.questions, new QuestionGroup(questionNode.title)))
          } else {
            results.questionNodes.push(Question.fromJson(questionNode, application.persistedAnswers))
          }
        })
        return results
      }
    }

    function validateBackend(application, success, error) {
      var newHakutoiveet = _(application.hakutoiveet).chain()
        .filter(function(hakutoive) { return hakutoive.addedDuringCurrentSession })
        .map(function(hakutoive) { return hakutoive.data["Koulutus-id"] })
        .compact().value().join(",")

      var questionQueryParam = newHakutoiveet.length > 0 ? "?questionsOf=" + newHakutoiveet : ""

      var responsePromise = $http.post("/omatsivut/secure/applications/validate/" + application.oid + questionQueryParam, application.toJson())
      responsePromise.success(function(data, status, headers, config) {
        if (data.errors.length === 0) {
          success({
            questions: getQuestions(data, application),
            response: data
          })
        } else {
          error({
            statusCode: 200,
            errors: data.errors,
            questions: getQuestions(data, application),
            response: data
          })
        }
      })

      responsePromise.error(function(data, status) {
        error({
          errors: [],
          statusCode: status,
          isSaveable: true,
          response: data
        })
      })
    }
  }])
}