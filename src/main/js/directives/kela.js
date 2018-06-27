import localize from '../localization';

export default function () {
  return {
    restrict: 'E',
    scope: {
      kela: '&'
    },
    template: require('./kela.html'),
    link: function (scope, element, attrs) {
      scope.localization = localize;
    }
  }
}
