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

        $scope.reserveStates = {
          LASNA_KOKO_LUKUVUOSI: 'semester',
          LASNA_SYKSY: 'autumn',
          LASNA: 'spring'
        };

        $scope.statesToReport = {
          LASNA_KOKO_LUKUVUOSI: 'semester',
          POISSA_KOKO_LUKUVUOSI: 'away', //*uusi, käännös oli
          EI_ILMOITTAUTUNUT: 'no_signup', //*uusi, käännös lisätty
          LASNA_SYKSY: 'autumn',
          POISSA_SYKSY: 'spring', //*uusi, huom. sama käännös
          LASNA: 'spring',
          POISSA: 'away_spring' //*uusi, käännös lisätty
        };

        $scope.linkkiOK = function(tulos) {
          return tulos.ilmoittautumistila.ilmoittautumistapa != null &&
              tulos.ilmoittautumistila.ilmoittautumistapa.url
        };

        $scope.ilmoittautunut = function(tulos) {
          if (tulos && tulos.ilmoittautumistila && tulos.ilmoittautumistila.ilmoittautumistila) {
            return $scope.reserveStates[tulos.ilmoittautumistila.ilmoittautumistila];
          } else return false;
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
          return localization('lasnaoloilmoittautuminen.' + $scope.reserveStates[tulos.ilmoittautumistila.ilmoittautumistila] );
        };

      }
    }
  }])
};
