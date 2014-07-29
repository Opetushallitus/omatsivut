var Hakemus = require('./hakemus')

module.exports = function(listApp) {
  listApp.controller("hakemusController", ["$scope", "$element", "$http", "applicationsResource", "applicationValidator", "settings", "debounce", function ($scope, $element, $http, applicationsResource, applicationValidator, settings, debounce) {
    applicationValidator = debounce(applicationValidator(), settings.modelDebounce)

    $scope.hasChanged = false
    $scope.isSaveable = true

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
        validateHakutoiveet()
      }
    }, true)

    $scope.$watch("application.getAnswerWatchCollection()", function(answers, oldAnswers) {
      if (!_.isEqual(oldAnswers, [])) {
        applicationChanged()
      }
    }, true)

    function applicationChanged() {
      $scope.hasChanged = true
      if ($scope.statusMessageType == "success")
        setStatusMessage("")
    }

    function validateHakutoiveet() {
      setStatusMessage("", "pending")
      applicationValidator($scope.application, success, error)
      $scope.isSaveable = false

      function success(data) {
        $scope.isSaveable = true
        setStatusMessage("")
        $scope.application.importQuestions(data.questions)
        updateValidationMessages([], true)
      }

      function error(data) {
        $scope.isSaveable = data.isSaveable
        setStatusMessage(data.errorText, "error")
        $scope.application.importQuestions(data.questions)
        if (data.questions)
          $scope.application.importQuestions(data.questions)
        updateValidationMessages(data.errors, true)
      }
    }

    function setStatusMessage(msg, type) {
      $scope.statusMessage = msg
      $scope.statusMessageType = type || ""
    }

    $scope.movePreference = function(from, to) {
      if (to >= 0 && to < this.application.hakutoiveet.length) {
        this.application.moveHakutoive(from, to)
        setStatusMessage()
      }
    }

    $scope.saveApplication = function() {
      applicationsResource.update({id: $scope.application.oid }, $scope.application.toJson(), onSuccess, onError)
      setStatusMessage("", "pending")

      function onSuccess(savedApplication) {
        $scope.$emit("highlight-save", $scope.application.getChangedItems())
        $scope.application.setAsSaved(savedApplication)
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
      }

      function validationError(data) {
        if (_.isArray(data) && data.length > 0)
          return "Ei tallennettu - vastaa ensin kaikkiin lisäkysymyksiin"
        else
          return "Tallentaminen epäonnistui"
      }
    }

    function updateValidationMessages(errors, skipQuestions) {
      var unhandledMessages = $scope.application.updateValidationMessages(errors, skipQuestions)
      if (unhandledMessages.length > 0) {
        _(unhandledMessages).each(function(item) {
          console.log("Validaatiovirhettä ei käsitelty:", item.questionId, item.errors)
        })

        setStatusMessage("Odottamaton virhe. Ota yhteyttä ylläpitoon.", "error")
      }
    }
  }])
}