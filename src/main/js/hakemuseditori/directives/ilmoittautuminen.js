export default function(app) {
  app.directive("ilmoittautuminen", [function () {
    return {
      restrict: 'E',
      scope: {
        hakukohteet: '&hakukohteet',
        oili: '&oili',
        application: '=application'
      },
      template: require('./ilmoittautuminen.html'),
      link: function (scope, element, attrs) {
        scope.localization = localize;

        scope.linkkiOK = function(tulos) {
          return tulos.ilmoittautumistila.ilmoittautumistapa != null &&
              tulos.ilmoittautumistila.ilmoittautumistapa.url
        }
      }
    }
  }])
};
