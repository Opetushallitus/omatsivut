var Hakemus = require('./hakemus')

module.exports = function(listApp) {
  listApp.controller("hakemusController", ["$scope", "$element", "$http", "applicationsResource", "applicationValidator", function ($scope, $element, $http, applicationsResource, applicationValidator) {
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
        $scope.$emit("applicationChange")
      }

      updateAdditionalQuestions()
    }, true)

    $scope.$watch("application.getAnswerWatchCollection()", function(answers, oldAnswers) {
      if (!_.isEqual(oldAnswers, {})) {
        $scope.$emit("applicationChange")
      }
    }, true)

    function updateAdditionalQuestions() {
      var application = $scope.application
      applicationValidator(application, function(questions) {
        $scope.additionalQuestions = questions
        application.setDefaultAnswers(questions)
      })
    }

    $scope.$on("applicationChange", function() {
      $scope.hasChanged = true
      setSaveMessage("")

      $scope.isValid = $scope.application.isValid()
      if (!$scope.isValid)
        setSaveMessage("Täytä kaikki tiedot", "error");
    })

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
        updateValidationMesssages([])
      }

      function onError(err) {
        switch (err.status) {
          case 400: setSaveMessage(validationError(err.data), "error"); updateValidationMesssages(err.data); break
          case 401: setSaveMessage("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.", "error"); break
          default: setSaveMessage("Tallentaminen epäonnistui", "error")
        }

        $scope.isSaving = false
      }

      function validationError(data) {
        if (_.isArray(data) && data.length > 0)
          return "Osa vastauksista puuttuu"
        else
          return "Tallentaminen epäonnistui"
      }

      function updateValidationMesssages(errors) { // TODO refactor
        var errorMap = _.reduce(errors, function(memo, error) {
          if (memo[error.key] == null)
            memo[error.key] = []
          memo[error.key].push(error.message)
          return memo
        }, {});

        (function updateErrors(node) {
          if (node.questionNodes == null) {

            node.validationMessage = (errorMap[node.question.id.questionId] || []).join(", ")
          } else {
            _(node.questionNodes).each(function(item) {
              updateErrors(item)
            })
          }
        })($scope.additionalQuestions)
      }
    };
  }])
}