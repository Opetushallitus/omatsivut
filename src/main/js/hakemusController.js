var Hakemus = require('./hakemus')
var util = require('./util')

module.exports = function(listApp) {
  listApp.controller("hakemusController", ["$scope", "$element", "$http", "applicationsResource", "applicationValidator", "settings", "debounce", function ($scope, $element, $http, applicationsResource, applicationValidator, settings, debounce) {
    applicationValidator = debounce(applicationValidator, settings.modelDebounce)

    $scope.hasChanged = false
    $scope.isSaving = false
    $scope.isValid = true

    $scope.timestampLabel = function() {
      if ($scope.application.received == $scope.application.updated)
        return "Hakemus jätetty"
      else
        return "Hakemusta muokattu"
    }

    $scope.$watch("application.getHakutoiveWatchCollection()", function(hakutoiveet, oldHakutoiveet) {
      // Skip initial values angular style
      if (!_.isEqual(hakutoiveet, oldHakutoiveet)) {
        applicationChanged()
      }

      $scope.isValid = $scope.application.validatePreferences()
      if ($scope.isValid)
        updateAdditionalQuestions()
      else
        setSaveMessage("Täytä kaikki tiedot", "error");
    }, true)

    $scope.$watch("application.getAnswerWatchCollection()", function(answers, oldAnswers) {
      if (!_.isEqual(oldAnswers, {})) {
        applicationChanged()
      }
    }, true)

    function updateAdditionalQuestions() {
      var application = $scope.application
      applicationValidator(application, success, error)

      function success(questions) {
        $scope.additionalQuestions = questions
        application.setDefaultAnswers(questions)
      }

      function error() {
        setSaveMessage("Tietojen haku epäonnistui. Yritä myöhemmin uudelleen.", "error")
      }
    }

    function applicationChanged() {
      $scope.hasChanged = true
      setSaveMessage("")
    }

    function setSaveMessage(msg, type) {
      $scope.saveMessage = msg
      $scope.saveMessageType = type
    }

    $scope.movePreference = function(from, to) {
      if (to >= 0 && to < this.application.hakutoiveet.length) {
        this.application.moveHakutoive(from, to)
        setSaveMessage()
      }
    }

    $scope.saveApplication = function() {
      $scope.isSaving = true;
      applicationsResource.update({id: $scope.application.oid }, $scope.application.toJson(), onSuccess, onError)

      function onSuccess(savedApplication) {
        $scope.$emit("highlight-items", $scope.application.getChangedItems())
        $scope.application.setAsSaved(savedApplication)
        $scope.isSaving = false
        $scope.hasChanged = false
        setSaveMessage("Kaikki muutokset tallennettu", "success")
        updateValidationMessages([])
      }

      function onError(err) {
        switch (err.status) {
          case 400:
            setSaveMessage(validationError(err.data), "error");
            updateValidationMessages(err.data);
            break
          case 401:
            setSaveMessage("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.", "error");
            break
          default:
            setSaveMessage("Tallentaminen epäonnistui. Yritä myöhemmin uudelleen.", "error")
        }

        $scope.isSaving = false
      }

      function validationError(data) {
        if (_.isArray(data) && data.length > 0)
          return "Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin"
        else
          return "Tallentaminen epäonnistui"
      }

      function updateValidationMessages(errors) {
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
    };
  }])
}