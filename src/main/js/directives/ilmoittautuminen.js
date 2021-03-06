import localize from '../localization';

export default function () {
  return {
      restrict: 'E',
      scope: {
        hakukohteet: '&',
        oili: '&',
        application: '='
      },
      template: require('./ilmoittautuminen.html'),
      link: function ($scope, element, attrs) {
        $scope.localization = localize;

        $scope.statesToReport = {
          LASNA_KOKO_LUKUVUOSI: 'semester',
          POISSA_KOKO_LUKUVUOSI: 'away',
          LASNA_SYKSY: 'autumn',
          POISSA_SYKSY: 'spring',
          LASNA: 'spring',
          POISSA: 'away_spring'
        };

        $scope.linkkiOK = function(tulos) {
          return tulos.ilmoittautumistila.ilmoittautumistapa != null &&
            tulos.ilmoittautumistila.ilmoittautumistapa.url
        };

        $scope.ilmoittautumistietoNaytetaan = function(tulos) {
          if (tulos && tulos.ilmoittautumistila && tulos.ilmoittautumistila.ilmoittautumistila) {
            return $scope.statesToReport[tulos.ilmoittautumistila.ilmoittautumistila];
          } else return false;
        };

        $scope.getEnrolmentMessageKeys = function(tulos) {
          var date = tulos.ilmoittautumisenAikaleima ? new Date(tulos.ilmoittautumisenAikaleima) : new Date();
          return {date: date.toLocaleDateString('fi-FI'), time: date.toLocaleTimeString('fi-FI')}
        };

        $scope.getStateTranslation = function(tulos) {
          return localize('lasnaoloilmoittautuminen.' + $scope.statesToReport[tulos.ilmoittautumistila.ilmoittautumistila] );
        };

        $scope.ohjeetUudelleOpiskelijalle = function(hakukohdeOid) {
          return $scope.application.ohjeetUudelleOpiskelijalle[hakukohdeOid];
        }
      }
  }
}


