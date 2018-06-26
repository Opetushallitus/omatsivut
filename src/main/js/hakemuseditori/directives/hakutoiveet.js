const _ = require('underscore');
const Hakutoive = require('../hakutoive');

module.exports = function(app) {
  app.directive("hakutoiveet", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        application: "=application",
        preferenceMoved: "=preferenceMoved",
        validating: "=validating"
      },
      template: require('./hakutoiveet.html'),

      link: function ($scope, element, attrs) {
        $scope.localization = localization

        $scope.movePreference = function(from, to) {
          if (to >= 0 && to < this.application.hakutoiveet.length) {
            this.application.movePreference(from, to)
            this.preferenceMoved()
          }
        }
      }
    }
  }])

  app.controller("additionalQuestionController", ["$scope", "localization", function($scope, localization) {
    $scope.localization = localization
    $scope.questionAnswered = function() {
      $scope.$emit("questionAnswered")
    }
  }])

  app.controller("hakutoiveController", ["$scope", "$timeout", "settings", "restResources", function($scope, $timeout, settings, restResources) {
    $scope.isEditingDisabled = function() { return !$scope.hakutoive.isNew || !$scope.application.isEditable($scope.$index) }

    $scope.isKoulutusSelectable = function() { return !$scope.isEditingDisabled() && this.hakutoive.hasOpetuspiste() && !_.isEmpty($scope.koulutusList) }

    $scope.isLoadingKoulutusList = function() { return !$scope.isEditingDisabled() && this.hakutoive.hasOpetuspiste() && _.isEmpty($scope.koulutusList) }

    $scope.showNotification = function(type) {
      var oid = $scope.hakutoive.data["Koulutus-id"]
      var notificationsForAo = $scope.application.notifications[oid] || {}
      return notificationsForAo[type]
    }

    $scope.isNonPrioritizedAndEditable = function () { return this.application.haku.usePriority || $scope.application.isEditable($scope.$index) }

    $scope.opetuspisteValittu = function($item, $model, $label) {
      this.hakutoive.setOpetuspiste($item.id, $item.name)
      $scope.koulutusList = []

      restResources.koulutukset.query({
        asId: this.application.haku.oid,
        opetuspisteId: $item.id,
        baseEducation: this.application.educationBackground.baseEducation,
        vocational: this.application.educationBackground.vocational,
        uiLang: $scope.localization("languageId")
      }, function(koulutukset) {
        $scope.koulutusList = koulutukset
        if (koulutukset.length === 1) {
          $scope.valittuKoulutus = koulutukset[0]
          $scope.hakutoive.setKoulutus(koulutukset[0])
        }
      })
    }

    $scope.opetuspisteModified = function() {
      if (_.isEmpty(this.hakutoive.data.Opetuspiste))
        this.hakutoive.clear()
      else
        this.hakutoive.removeOpetuspisteData()
    }

    $scope.removeHakutoive = function(index) {
      $scope.application.removePreference(index)
      $scope.application.addPreference(new Hakutoive({}))
    }

    $scope.canRemovePreference = function(index) {
      return $scope.application.hasPreference(index) && !$scope.application.preferenceLocked(index)
    }

    $scope.koulutusValittu = function(index) {
      this.hakutoive.setKoulutus(this["valittuKoulutus"])
    }

    $scope.findOpetuspiste = function(val) {
      return restResources.opetuspisteet.query({
        query: val,
        asId: $scope.application.haku.oid,
        lang: $scope.localization("languageId")
      }).$promise
    };
  }])
}