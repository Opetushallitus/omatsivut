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
      console.log('linking', scope);
      scope.localization = localize;
      scope.justSomeToken = 'c d e f g'
      scope.migriTokenParsed = scope.token || 'no token found';
      scope.constructUrl = function() {
        const base = "https://opintopolkumigri.fi?token=";
        const localized = scope.localization('migri.linkki-pohja');
        console.log("Getting migri url for base " + (localized || base) + ". Token: " + scope.migriTokenParsed);
        scope.parsedUrl = (localized || base) + scope.migriTokenParsed;
        return (localized || base) + scope.migriTokenParsed;
      }
      scope.constructUrl();
    }
  }
}
