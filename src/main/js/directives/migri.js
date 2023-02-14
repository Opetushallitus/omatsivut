import localize from '../localization';

export default function () {
  return {
    restrict: 'E',
    scope: {
      migri: '&'
    },
    template: require('./migri.html'),
    link: function (scope, element, attrs) {
      scope.localization = localize;
    }
  }
}
