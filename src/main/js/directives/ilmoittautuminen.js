module.exports = function(listApp) {
  listApp.directive("ilmoittautuminen", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        hakukohteet: '&hakukohteet'
      },
      templateUrl: 'templates/ilmoittautuminen.html',
      link: function (scope, element, attrs) {
        scope.localization = localization
      }
    }
  }])
}