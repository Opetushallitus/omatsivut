module.exports = function(app) {
  app.directive("ilmoittautuminen", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        hakukohteet: '&hakukohteet',
        oili: '&oili'
      },
      templateUrl: 'ilmoittautuminen.html',
      link: function (scope, element, attrs) {
        scope.localization = localization;
      }
    }
  }])
};