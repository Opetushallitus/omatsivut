import localize from '../../js/localization';

export default ["restResources", function (restResources) {
  return {
    restrict: 'E',
    scope: {
      application: '=',
      tulos: '='
    },
    template: require('./lasnaoloilmoittautuminen.html'),
    link: function ($scope, element, attrs) {
      $scope.localization = localize;
      $scope.states = {
        // Spring hakus
        semester: 'LASNA_KOKO_LUKUVUOSI',
        autumn: 'LASNA_SYKSY',

        // Autumn hakus
        spring: 'LASNA'
      };
      $scope.reserveStates = {
        LASNA_KOKO_LUKUVUOSI: 'semester',
        LASNA_SYKSY: 'autumn',
        LASNA: 'spring'
      };

      $scope.init = function() {
        var hakutoive = $scope.application.hakutoiveet.find(function(element) {
            return element.data['Koulutus-id'] === $scope.tulos.hakukohdeOid;
        });

        $scope.tulos.koulutuksenAlkaminen = hakutoive.koulutuksenAlkaminen ? hakutoive.koulutuksenAlkaminen : { kausiUri: 'kausi_s#1' };

        $scope.ilmoittautuminen = $scope.reserveStates[$scope.tulos.ilmoittautumistila.ilmoittautumistila];
        $scope.ilmoittautunut = !!$scope.ilmoittautuminen;
      }();

      $scope.postLasnaoloilmoittautuminen = function() {
        $scope.error = false;
        var body = {
            hakukohdeOid: $scope.tulos.hakukohdeOid,
            muokkaaja: '',
            tila: $scope.states[$scope.ilmoittautuminen],
            selite: 'Omien sivujen läsnäoloilmoittautuminen'
        };
        restResources.lasnaoloilmoittautuminen.save({ hakuOid: $scope.application.haku.oid , hakemusOid: $scope.application.oid }, JSON.stringify(body), onSuccess, onError);
      };

      $scope.getStateTranslation = function() {
        return localize('lasnaoloilmoittautuminen.' + $scope.ilmoittautuminen);
      };

      $scope.getErrorTranslation = function() {
          return localize('lasnaoloilmoittautuminen.error.' + $scope.error);
      };

      $scope.schoolStartsInSpring = function () {
        return $scope.tulos.koulutuksenAlkaminen.kausiUri !== null
          && $scope.tulos.koulutuksenAlkaminen.kausiUri.startsWith('kausi_k');
      };

      $scope.schoolStartsInAutumn = function () {
        return $scope.tulos.koulutuksenAlkaminen.kausiUri !== null
          && $scope.tulos.koulutuksenAlkaminen.kausiUri.startsWith('kausi_s');
      };

      function onSuccess() {
        $scope.tulos.ilmoittautumistila.ilmoittautumisaika.tehty = new Date();
        $scope.ilmoittautunut = true;
      }

      function onError(err) {
        console.log(err);
        switch (err.status) {
          case 401:
            document.location.replace(window.url("omatsivut.login"));
            break;
          case 404:
            $scope.error = 'notFound';
            break;
          default:
            $scope.error = 'other';
        }
      }

      $scope.getEnrolmentMessageKeys = function() {
        const date = $scope.tulos.ilmoittautumisenAikaleima ? new Date($scope.tulos.ilmoittautumisenAikaleima) : new Date();
        return {date: date.toLocaleDateString('fi-FI'), time: date.toLocaleTimeString('fi-FI')}
      };
    }
  }
}]
