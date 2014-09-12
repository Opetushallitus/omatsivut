var Hakemus = require("./hakemus")
var Hakutoive = require("./hakutoive")

module.exports = function(listApp) {
  listApp.controller("hakemusController", ["$scope", "$element", "$http", "$sce", "restResources", "applicationValidator", "settings", "debounce", "localization", function ($scope, $element, $http, $sce, restResources, applicationValidator, settings, debounce, localization) {
    applicationValidator = debounce(applicationValidator(), settings.modelDebounce)

    $scope.applicationPeriod = $scope.application.haku.applicationPeriods[0]
    $scope.hasChanged = false
    $scope.isSaveable = true
    $scope.isValidating = false

    $scope.formatTimestamp = function(dt) {
      return moment(dt).format('LLL').replace(/,/g, "")
    }

    $scope.formatApplicationPeriod = function(dt) {
      return moment(dt).format('LLLL').replace(/,/g, "")
    }

    $scope.formatDate = function(dt) {
      if (dt == null)
        return ""
      else
        return moment(dt).format('LL').replace(/,/g, "")
    }

    $scope.timestampLabel = function() {
      if ($scope.application.received == $scope.application.updated)
        return localization("label.applicationReceived")
      else
        return localization("label.applicationUpdated")
    }

    $scope.resultState = getResultState($scope.application)

    function getResultState(application) {
      if (application.state.valintatulos) { //TODO replace dummy implementation
        var hakutoive = _(application.state.valintatulos.hakutoiveet).find(function(hakutoive) { return hakutoive.vastaanottotila == "VASTAANOTTANUT" })
        if (hakutoive != null)
          return localization("message.resultState.Vastaanottanut", {
            opiskelupaikka: hakutoive.opetuspiste.name + " - " + hakutoive.koulutus.name
          })
      }
    }

    function underscoreToCamelCase(str) {
      return str.toLowerCase().replace(/^(.)|_(.)/g, function(match, char1, char2) {
        return char1!=null ? char1.toUpperCase() : char2.toUpperCase()
      })
    }

    $scope.valintatulosText = function(valintatulos) {
      var tila = underscoreToCamelCase(valintatulos.tila)
      var localizationString = (tila=== "Varalla" && valintatulos.varasijojaTaytetaanAsti != null) ? "label.resultState.VarallaPvm" : "label.resultState." + tila
      return localization(localizationString, {
        varasija: valintatulos.varasijanumero,
        varasijaPvm: $scope.formatDate(valintatulos.varasijojaTaytetaanAsti)
      })
    }

    $scope.valintatulosColor = function(valintatulos) {
      var tila = underscoreToCamelCase(valintatulos.tila)
      if (tila == "Hyvaksytty" || tila == "HarkinnanvaraisestiHyvaksytty")
        return "green"
      else if (tila == "Hylatty" || tila == "Perunut" || tila == "Peruutettu")
        return "gray"
      else if (tila == "Kesken" || tila == "Varalla")
        return "blue"
      else
        return "transparent lighter italic"
    }

    $scope.$watch("application.getHakutoiveWatchCollection()", function(hakutoiveet, oldHakutoiveet) {
      // Skip initial values angular style
      if (!_.isEqual(hakutoiveet, oldHakutoiveet)) {
        applicationChanged()
        validateHakutoiveet(true)
      }
    }, true)

    $scope.$watch("application.getAnswerWatchCollection()", function(answers, oldAnswers) {
      if (!_.isEqual(oldAnswers, [])) {
        applicationChanged()
      }
    }, true)

    $scope.$on("questionAnswered", function() {
        validateHakutoiveet(false)
    })

    function applicationChanged() {
      $scope.hasChanged = true
      if ($scope.statusMessageType == "success")
        setStatusMessage("")
    }

    function validateHakutoiveet(skipQuestions) {
      applicationValidator($scope.application, beforeBackendValidation, success, error)

      function beforeBackendValidation() {
        setValidatingIndicator(true)
      }

      function success(data) {
        setStatusMessage(localization("message.validationOk"), "info")
        $scope.isSaveable = true
        setValidatingIndicator(false)
        $scope.application.mergeValidationResult(data)
        updateValidationMessages([], skipQuestions)
      }

      function error(data) {
        setValidatingIndicator(false)
        if (!data.statusCode) { // validointi epäonnistui frontendissä
          $scope.isSaveable = false
          setStatusMessage(localization("error.validationFailed"), "error")
        } else if (data.statusCode === 200) {
          $scope.isSaveable = !Hakutoive.hasHakutoiveErrors(data.errors)
          setStatusMessage(localization("error.validationFailed"), "error")
        } else if (data.statusCode == 401) {
          $scope.isSaveable = true
          setStatusMessage(localization("error.sessionExpired"), "error")
        } else if (data.statusCode == 500) {
          $scope.isSaveable = true
          setStatusMessage(localization("error.serverError"), "error")
        } else {
          $scope.isSaveable = false
          setStatusMessage(localization("error.validationFailed_httpError"), "error")
        }

        var updateQuestions = data.questions != null && !Hakutoive.hasHakutoiveErrors(data.errors)

        if (updateQuestions) // frontside validation does not include questions -> don't update // TODO: testikeissi tälle (vastaa kysymykseen, aiheuta fronttivalidaatiovirhe)
          $scope.application.mergeValidationResult(data)

        updateValidationMessages(data.errors, skipQuestions)
      }
    }

    function setStatusMessage(msg, type) {
      $scope.statusMessage = msg
      $scope.statusMessageType = type || ""
    }

    var setValidatingIndicator = debounce(function(isVisible) {
      $scope.isValidating = isVisible
    }, settings.uiIndicatorDebounce)

    $scope.movePreference = function(from, to) {
      if (to >= 0 && to < this.application.hakutoiveet.length) {
        this.application.movePreference(from, to)
        setStatusMessage()
      }
    }

    $scope.saveApplication = function() {
      restResources.applications.update({id: $scope.application.oid }, $scope.application.toJson(), onSuccess, onError)
      setStatusMessage("", "pending")

      function onSuccess(savedApplication) {
        $scope.$emit("highlight-save", $scope.application.getChangedItems())
        $scope.application.mergeSavedApplication(savedApplication)
        $scope.hasChanged = false
        setStatusMessage(localization("message.changesSaved"), "success")
        updateValidationMessages([])

        $scope.$broadcast("show-callout", "attachments", savedApplication.requiresAdditionalInfo === true)
      }

      function onError(err) {
        var saveError = (function() {
          if (err.status == 400 && (_.isArray(err.data) && err.data.length > 0))
            return "error.saveFailed_validationError"
          else if (err.status == 400 && !(_.isArray(err.data) && err.data.length > 0))
            return "error.serverError"
          else if (err.status == 401)
            return "error.saveFailed_sessionExpired"
          else if (err.status == 500)
            return "error.serverError"
          else
            return "error.saveFailed"
        })()

        setStatusMessage(localization(saveError), "error")
        if (err.status == 400) // Validointivirhe
          updateValidationMessages(err.data)
      }
    }

    function updateValidationMessages(errors, skipQuestions) {
      var unhandledMessages = $scope.application.updateValidationMessages(errors, skipQuestions)
      unhandledMessages = hideErrorIfAlreadyShowsKoulutusError(unhandledMessages)

      if (unhandledMessages.length > 0) {
        _(unhandledMessages).each(function(item) {
          console.log("Validaatiovirhettä ei käsitelty:", item.questionId, item.errors)
        })

        setStatusMessage(localization("error.serverError"), "error")
      }

      function hideErrorIfAlreadyShowsKoulutusError(messages) {
        return _(messages).filter(function(message) {
          var index = Hakutoive.parseHakutoiveIndex(message.questionId)
          var relatedErrorShown = _(errors).any(function(error) {
            return Hakutoive.isHakutoiveError(error.key) && Hakutoive.parseHakutoiveIndex(error.key) == index
          })
          return !relatedErrorShown
        })
      }
    }
  }])
}