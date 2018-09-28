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

        $scope.linkkiOK = function(tulos) {
          return tulos.ilmoittautumistila.ilmoittautumistapa != null &&
              tulos.ilmoittautumistila.ilmoittautumistapa.url
        }

        $scope.ilmoittautunut = function(tulos) {
          return $scope.reserveStates[tulos.ilmoittautumistila.ilmoittautumistila];
        }
      }
    }
  }])
};
