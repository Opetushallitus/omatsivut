var Hakemus = require("../hakemus")
var Hakutoive = require("../hakutoive")
var util = require("../util")
var Question = require("../question").Question

module.exports = function(app) {
  app.directive("application", ["$sce", "restResources", "applicationValidator", "settings", "debounce", "localization", "$timeout", function ($sce, restResources, applicationValidator, settings, debounce, localization, $timeout) {
    return {
      restrict: 'E',
      scope: {
        application: "=application"
      },
      templateUrl: 'templates/application.html',

      link: function ($scope, $element, attrs) {
        $scope.localization = localization
        var applicationValidatorBounced = debounce(applicationValidator(), settings.modelDebounce)
        $scope.isSaveable = true
        $scope.isValidating = false

        $scope.timestampLabel = function() {
          if ($scope.application.received == $scope.application.updated)
            return localization("label.applicationReceived")
          else
            return localization("label.applicationUpdated")
        }
        $scope.shouldSaveButtonBeDisabled = function() {
          return $scope.applicationForm.$pristine || $scope.statusMessageType=='pending' || !$scope.isSaveable || $scope.isValidating
        }

        $scope.statusMessageStyleModifier = function() {
          return {'ajax-spinner': $scope.statusMessageType=='pending', error: $scope.statusMessageType=='error', success: $scope.statusMessageType == 'success'}
        }

        function getHakutoiveet() {
          return _($scope.application.hakutoiveet).map(function(hakutoive) {
            return {
              "Koulutus": hakutoive.data["Koulutus"],
              "Koulutus-id": hakutoive.data["Koulutus-id"],
              "Opetuspiste": hakutoive.data["Opetuspiste"],
              "Opetuspiste-id": hakutoive.data["Opetuspiste-id"]
            }
          })
        }

        function getAnswers() {
          var answersToAdditionalQuestions = _(Question.questionMap($scope.application.additionalQuestions)).map(function(item, key) { return item.answer })
          var otherAnswers = _($scope.application.henkilotiedot).map(function(item) { return item.answer })
          return answersToAdditionalQuestions.concat(otherAnswers)
        }

        $scope.$watch(getHakutoiveet, function(hakutoiveet, oldHakutoiveet) {
          // Skip initial values angular style
          if (!_.isEqual(hakutoiveet, oldHakutoiveet)) {
            applicationChanged()
            validateHakutoiveet(true)
          }
        }, true)

        $scope.$watch(getAnswers, function(answers, oldAnswers) {
          if (!_.isEqual(oldAnswers, answers)) {
            applicationChanged()
          }
        }, true)

        $scope.$on("questionAnswered", function() {
          validateHakutoiveet(false)
        })

        $scope.hakutoiveVastaanotettu = function(hakutoive, updated) {
          $scope.application.mergeSavedApplication(updated.hakemus)
          $timeout(function() {
            $scope.$emit("hakutoive-vastaanotettu", hakutoive)
          }, 0)
        }

        $scope.url = window.url;

        function applicationChanged() {
          $scope.applicationForm.$setDirty()
          if ($scope.statusMessageType == "success")
            setStatusMessage("")
        }

        function validateHakutoiveet(skipQuestions) {
          applicationValidatorBounced($scope.application, beforeBackendValidation, success, error)

          function beforeBackendValidation() {
            setValidatingIndicator(true)
          }

          function success(data) {
            setStatusMessage(localization("message.validationOk"), "info")
            $scope.isSaveable = true
            setValidatingIndicator(false)
            $scope.application.importQuestions(data.questions)
            $scope.application.importHakuajat(data.response.hakemus.hakutoiveet)
            $scope.application.importPaymentInfo(data.response.paymentInfo)
            $scope.application.notifications = data.response.hakemus.notifications
            $scope.application.tuloskirjeet = data.response.hakemus.tuloskirjeet
            updateValidationMessages([], skipQuestions)
          }

          function error(data) {
            setValidatingIndicator(false)
            if (!data.statusCode) { // validointi epäonnistui frontendissä
              $scope.isSaveable = false
              setStatusMessage(localization("error.validationFailed"), "error")
            } else if (data.statusCode === 200) {
              $scope.isSaveable = _.isEmpty(data.errors)
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
            var errors = data.errors

            if (updateQuestions) {// frontside validation does not include questions -> don't update
              $scope.application.importQuestions(data.questions)
            } else {
              errors = _(data.errors).filter(function (error) { return Hakutoive.isHakutoiveError(error.key) })
            }
            if (data.response != null && data.response.hakemus != null) {
              $scope.application.importHakuajat(data.response.hakemus.hakutoiveet)
              $scope.application.notifications = data.response.hakemus.notifications
              $scope.application.tuloskirjeet = data.response.hakemus.tuloskirjeet
            }

            updateValidationMessages(errors, skipQuestions)
          }
        }

        $scope.preferenceMoved = function() {
          setStatusMessage("")
        }

        function setStatusMessage(msg, type) {
          $scope.statusMessage = msg
          $scope.statusMessageType = type || ""
        }

        var setValidatingIndicator = debounce(function(isVisible) {
          $scope.isValidating = isVisible
        }, settings.uiIndicatorDebounce)

        $scope.saveApplication = function() {
          restResources.applications.update({id: $scope.application.oid }, $scope.application.toJson(), onSuccess, onError)
          setStatusMessage("", "pending")

          function onSuccess(savedApplication) {
            highlightSavedItems($scope.application.getChangedPreferences())
            $scope.$broadcast("show-callout", "attachments", savedApplication.requiresAdditionalInfo === true && $scope.application.getChangedPreferences().length > 0)
            $scope.application.mergeSavedApplication(savedApplication)
            $scope.applicationForm.$setPristine()
            setStatusMessage(localization("message.changesSaved"), "success")
            updateValidationMessages([])
            if($scope.application.editHakutoiveetEnabled()) scrollToTop()
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

        function highlightSavedItems(indexes) {
          var items = $element.find(".preference-list-item")

          _.each(indexes, function(index) {
            items.eq(index).addClass("saved")
          })

          $element.find(".timestamp-row").addClass("saved")
          $element.find(".yhteystiedot .ng-dirty").addClass("saved") // edited contact details

          window.setTimeout(function() {
            $element.find(".saved").removeClass("saved")
            $(".timestamp-row").removeClass("saved")
          }, 3000)
        }

        function scrollToTop() {
          var $applicationHeader = $($element[0]).find(".application-header")
          if ($applicationHeader.get(0) != null && $(window).scrollTop() > $applicationHeader.get(0).offsetTop) $applicationHeader.get(0).scrollIntoView()
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
      }
    }
  }])
}
