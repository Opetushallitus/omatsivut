var Hakemus = require('./hakemus')
var util = require('./util')

module.exports = function(listApp) {
  listApp.controller("hakemusController", ["$scope", "$element", "$http", "applicationsResource", "applicationValidator", "applicationFormatter", "settings", "debounce", "domainUtil", function ($scope, $element, $http, applicationsResource, applicationValidator, applicationFormatter, settings, debounce, domainUtil) {
    applicationValidator = debounce(applicationValidator, settings.modelDebounce)

    $scope.hasChanged = false
    $scope.isSaving = false
    $scope.isSaveable = true

    $scope.timestampLabel = function() {
      if ($scope.application.received == $scope.application.updated)
        return "Hakemus jätetty"
      else
        return "Hakemusta muokattu"
    }

    $scope.$watch("application.getHakutoiveWatchCollection()", function(hakutoiveet, oldHakutoiveet) {
      // Skip initial values angular style
      if (!_.isEqual(hakutoiveet, oldHakutoiveet))
        applicationChanged()

      validateHakutoiveet()
    }, true)

    $scope.$watch("application.getAnswerWatchCollection()", function(answers, oldAnswers) {
      if (!_.isEqual(oldAnswers, {}))
        applicationChanged()
    }, true)

    function applicationChanged() {
      $scope.hasChanged = true
      setStatusMessage("")
    }

    function validateHakutoiveet() {
      applicationValidator($scope.application, $scope.additionalQuestions, success, error)
      $scope.isSaveable = false

      function success(data) {
        $scope.isSaveable = true
        setStatusMessage("")
        importQuestions(data.questions)
        updateHakutoiveValidationMessages([])
      }

      function error(data) {
        $scope.isSaveable = data.isSaveable
        setStatusMessage(data.errorText, "error")
        importQuestions(data.questions)
        updateHakutoiveValidationMessages(data.errors) // Lisäkysymysten virheet näytetään vasta tallennuksen yhteydessä
        updateMiscValidationMessages(data.errors)
      }
    }

    function importQuestions(questions) {
      $scope.additionalQuestions = questions
      $scope.application.setDefaultAnswers(questions)
    }

    function setStatusMessage(msg, type) {
      $scope.saveMessage = msg
      $scope.saveMessageType = type
    }

    $scope.movePreference = function(from, to) {
      if (to >= 0 && to < this.application.hakutoiveet.length) {
        this.application.moveHakutoive(from, to)
        setStatusMessage()
      }
    }

    $scope.saveApplication = function() {
      $scope.isSaving = true;
      applicationsResource.update({id: $scope.application.oid }, applicationFormatter($scope.application, $scope.additionalQuestions), onSuccess, onError)
      setStatusMessage("", "")

      function onSuccess(savedApplication) {
        $scope.$emit("highlight-save", $scope.application.getChangedItems())
        $scope.application.setAsSaved(savedApplication)
        $scope.isSaving = false
        $scope.hasChanged = false
        setStatusMessage("Kaikki muutokset tallennettu", "success")
        updateValidationMessages([])
      }

      function onError(err) {
        switch (err.status) {
          case 400:
            setStatusMessage(validationError(err.data), "error")
            updateValidationMessages(err.data)
            break
          case 401:
            setStatusMessage("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.", "error");
            break
          default:
            setStatusMessage("Tallentaminen epäonnistui. Yritä myöhemmin uudelleen.", "error")
        }

        $scope.isSaving = false
      }

      function validationError(data) {
        if (_.isArray(data) && data.length > 0)
          return "Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin"
        else
          return "Tallentaminen epäonnistui"
      }
    }

    function updateValidationMessages(errors) {
      updateQuestionValidationMessages(errors)
      updateHakutoiveValidationMessages(errors)
      updateMiscValidationMessages(errors)
    }

    function updateHakutoiveValidationMessages(errors) {
      var errorMap = util.mapArray(errors, "key", "message");
      $scope.application.hakutoiveet.forEach(function(hakutoive, index) {
        var errorKeys = ["preference" + (index+1) + "-Koulutus", "preference" + (index+1)]
        var errors = _(errorKeys).chain()
          .map(function(errorKey) { return errorMap[errorKey] })
          .flatten()
          .without(undefined)
          .value()
        hakutoive.setErrors(errors)
      })
    }

    function updateQuestionValidationMessages(errors) {
      var errorMap = util.mapArray(errors, "key", "message");
      (function updateErrors(node) {
        if (node != null) {
          if (node.questionNodes == null) {
            node.validationMessage = (errorMap[node.question.id.questionId] || []).join(", ")
          } else {
            _(node.questionNodes).each(updateErrors)
          }
        }
      })($scope.additionalQuestions)
    }

    function updateMiscValidationMessages(errors) {
      var questionKeys = (function getQuestionKeys(node, list) {
        if (node != null) {
          if (node.questionNodes == null)
            list.push(node.question.id.questionId)
          else
            _(node.questionNodes).each(function(subnode) { getQuestionKeys(subnode, list) })
        }
        return list
      })($scope.additionalQuestions, [])

      var miscErrors = _(errors).filter(function(error) {
        return _(questionKeys).find(function(key) { return key === error.key }) == null &&
          !domainUtil.isHakutoiveError(error.key)
      })

      if (miscErrors.length > 0) {
        setStatusMessage("Odottamaton virhe. Ota yhteyttä ylläpitoon.", "error")
        console.log("Käsittelemättömät validointivirheet:", _(miscErrors).map(JSON.stringify).join(", "))
      }
    }
  }])
}