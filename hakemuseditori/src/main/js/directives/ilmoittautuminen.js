module.exports = function(app) {
  app.directive("ilmoittautuminen", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        hakukohteet: '&hakukohteet'
      },
      templateUrl: 'templates/ilmoittautuminen.html',
      link: function (scope, element, attrs) {
        scope.localization = localization;

        scope.getIlmoittautuminenUrl = function(tulos) {
          return tulos.ilmoittautumistila.ilmoittautumistapa.url + '?token=' + window.sessionStorage.getItem('bearerToken');
        }
      }
    }
  }])
};