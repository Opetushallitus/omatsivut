module.exports = function(app) {
  app.directive("kela", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        kela: '&kela'
      },
      templateUrl: 'templates/kela.html',
      link: function (scope, element, attrs) {
        scope.localization = localization
      }
    }
  }])
}