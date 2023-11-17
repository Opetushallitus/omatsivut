import localize from '../localization';

export default function () {
  return {
    restrict: 'E',
    scope: {
      migri: '&migri',
      token: '@token'
    },
    template: require('./migri.html'),
    link: function (scope, element, attrs) {
      scope.localization = localize;
      scope.tokenParam = scope.token ? "?token=" + scope.token : ""
      scope.parsedUrl = scope.localization('migri.linkki-pohja') + scope.tokenParam;
    }
  }
}
