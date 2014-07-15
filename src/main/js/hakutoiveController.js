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

      $timeout(function() {
        $scope.application.addPreference(new Hakutoive({}))
      }, settings.uiTransitionTime)
    }

    $scope.canRemovePreference = function(index) {
      if (index === 0)
        return $scope.application.hasPreference(1)
      else
        return $scope.application.hasPreference(index)
    }

    $scope.koulutusValittu = function(index) {
      this.hakutoive.setKoulutus(this["valittuKoulutus"])
    }

    function findKoulutukset(applicationOid, opetuspisteId, educationBackground) {
      return $http.get("https://testi.opintopolku.fi/ao/search/" + applicationOid + "/" + opetuspisteId, {
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
      return $http.get('https://testi.opintopolku.fi/lop/search/' + val, {
        params: {
          asId: '1.2.246.562.5.2014022711042555034240'
        }
      }).then(function(res){
        return res.data;
      });
    };
  }])
}