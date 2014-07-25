var QuestionItem = require('./additionalQuestion').AdditionalQuestion
var QuestionGroup = require('./additionalQuestion').AdditionalQuestionGroup
var util = require('./util')

module.exports = function(listApp) {
  listApp.factory("domainUtil", [function() {
    var hakutoiveErrorRegexp = /^(preference\d)$|^(preference\d)-Koulutus$/
    return {
      isHakutoiveError: function(questionId) {
        return hakutoiveErrorRegexp.test(questionId)
      },

      questionMap: function(questions) {
        questions = util.flattenTree(questions, "questionNodes")
        return util.indexBy(questions, function(node) { return node.question.id.questionId })
      },

      hakutoiveMap: function(hakutoiveet) {
        return util.indexBy(hakutoiveet, function(hakutoive, index) { return "preference" + (index+1) })
      },

      questionIdToHakutoiveId: function(questionId) {
        return _.chain(hakutoiveErrorRegexp.exec(questionId)).rest().without(undefined).first().value()
      }
    }
  }])

  listApp.factory("applicationValidator", ["$http", "applicationFormatter", "domainUtil", function($http, applicationFormatter, domainUtil) {
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

    return function(application, questions, success, error) {
      var preferencesValid = application.validatePreferences()

      if (preferencesValid) {
        validateBackend(application, questions, success, error)
      } else {
        error({
          errorText: "Täytä kaikki tiedot",
          errors: [],
          isSaveable: false
        })
      }
    }

    var currentRequest

    function validateBackend(application, questions, success, error) {
      currentRequest = {}
      var thisRequest = currentRequest
      var responsePromise = $http.post("/omatsivut/api/applications/validate/" + application.oid, applicationFormatter(application, questions))
      responsePromise.success(function(data, status, headers, config) {
        if (currentRequest === thisRequest) {
          if (data.errors.length === 0) {
            success({
              questions: getQuestions(data)
            })
          } else {
            error({
              isSaveable: !hasHakutoiveErrors(data.errors),
              errorText: "Täytä kaikki tiedot",
              errors: data.errors,
              questions: getQuestions(data)
            })
          }
        }
      })

      responsePromise.error(function(data, status) {
        error({
          errorText: "Tietojen haku epäonnistui. Yritä myöhemmin uudelleen.",
          errors: []
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