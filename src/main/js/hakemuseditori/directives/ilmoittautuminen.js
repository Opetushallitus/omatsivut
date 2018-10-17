module.exports = function(app) {
  app.directive("ilmoittautuminen", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        hakukohteet: '&hakukohteet',
        oili: '&oili',
        application: '=application'
      },
      templateUrl: 'ilmoittautuminen.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization;

        $scope.statesToReport = {
          LASNA_KOKO_LUKUVUOSI: 'semester',
          POISSA_KOKO_LUKUVUOSI: 'away',
          EI_ILMOITTAUTUNUT: 'no_signup',
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
            console.log("tila: " + tulos.ilmoittautumistila.ilmoittautumistila + ", bool: " + $scope.statesToReport[tulos.ilmoittautumistila.ilmoittautumistila]);
            return $scope.statesToReport[tulos.ilmoittautumistila.ilmoittautumistila];
          } else return false;
        };

        $scope.getEnrolmentMessageKeys = function(tulos) {
          var date = tulos.ilmoittautumisenAikaleima ? new Date(tulos.ilmoittautumisenAikaleima) : new Date();
          return {date: date.toLocaleDateString('fi-FI'), time: date.toLocaleTimeString('fi-FI')}
        };

        $scope.getStateTranslation = function(tulos) {
          return localization('lasnaoloilmoittautuminen.' + $scope.statesToReport[tulos.ilmoittautumistila.ilmoittautumistila] );
        };

      }
    }
  }])
};
