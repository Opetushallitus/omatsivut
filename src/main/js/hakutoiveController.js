var Hakutoive = require('./hakutoive')

module.exports = function(listApp) {
  listApp.controller("hakutoiveController", ["$scope", "$http", "$timeout", "settings", function($scope, $http, $timeout, settings) {
    $scope.isEditingDisabled = function() { return !$scope.hakutoive.isNew || !$scope.application.isEditable($scope.$index) }

    $scope.isKoulutusSelectable = function() { return !$scope.isEditingDisabled() && this.hakutoive.hasOpetuspiste() && !_.isEmpty($scope.koulutusList) }

    $scope.isLoadingKoulutusList = function() { return !$scope.isEditingDisabled() && this.hakutoive.hasOpetuspiste() && _.isEmpty($scope.koulutusList) }

    $scope.opetuspisteValittu = function($item, $model, $label) {
      this.hakutoive.setOpetuspiste($item.id, $item.name)

      findKoulutukset(this.application.haku.oid, $item.id, this.application.educationBackground)
        .then(function(koulutukset) { $scope.koulutusList = koulutukset; })
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
      return $scope.application.hasPreference(index)
    }

    $scope.koulutusValittu = function(index) {
      this.hakutoive.setKoulutus(this["valittuKoulutus"])
    }

    function findKoulutukset(asId, opetuspisteId, educationBackground) {
      return $http.get("koulutusinformaatio/koulutukset/" + asId + "/" + opetuspisteId, {
        params: {
          baseEducation: educationBackground.baseEducation,
          vocational: educationBackground.vocational,
          uiLang: "fi" // TODO: kieliversio
        }
      }).then(function(res){
        return res.data;
      });
    }

    $scope.findOpetuspiste = function(val) {
      return $http.get('koulutusinformaatio/opetuspisteet/' + val, {
        params: {
          asId: $scope.application.haku.oid
        }
      }).then(function(res){
        return res.data;
      });
    };
  }])
}