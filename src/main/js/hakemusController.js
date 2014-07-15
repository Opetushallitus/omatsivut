var Hakemus = require('./hakemus')
module.exports = function(listApp) {
  listApp.controller("hakemusController", ["$scope", "$element", "$http", "applicationsResource", function ($scope, $element, $http, applicationsResource) {
    $scope.hasChanged = false
    $scope.isSaving = false
    $scope.isValid = true

    $scope.$watch("application.getHakutoiveWatchCollection()", function(hakutoiveet, oldHakutoiveet) {
      // Skip initial values angular style
      if (!_.isEqual(hakutoiveet, oldHakutoiveet)) {
        $scope.$emit("applicationChange")
      }

      updateQuestions()
    }, true)

    $scope.$watch("application.getAnswerWatchCollection()", function(answers, oldAnswers) {
      if (oldAnswers != null) {
        $scope.$emit("applicationChange")
      }
    }, true)

    function updateQuestions() {
      var application = $scope.application
      var responsePromise = $http.post("api/applications/validate/" + application.oid, application.toJson());
      responsePromise.success(function(data, status, headers, config) {
        application.updateQuestions(data)
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

      function onSuccess() {
        $scope.$emit("highlight-items", $scope.application.getChangedItems());
        $scope.application.setAsSaved();
        $scope.isSaving = false;
        $scope.hasChanged = false
        setSaveMessage("Kaikki muutokset tallennettu", "success");
      }

      function onError(err) {
        switch (err.status) {
          case 401: setSaveMessage("Tallentaminen epäonnistui, sillä istunto on vanhentunut. Kirjaudu uudestaan sisään.", "error"); break
          default: setSaveMessage("Tallentaminen epäonnistui", "error")
        }

        $scope.isSaving = false
        console.log(err)
      }
    };
  }])
}