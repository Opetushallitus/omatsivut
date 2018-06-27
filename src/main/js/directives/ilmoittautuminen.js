import localize from '../localization';

export default [function () {
  return {
    restrict: 'E',
    scope: {
      hakukohteet: '&',
      oili: '&',
      application: '='
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
}]
