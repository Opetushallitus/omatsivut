module.exports = function(app) {
  app.directive("kela", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        kelaURL: '&kelaURL'
      },
      templateUrl: 'templates/kela.html',
      link: function (scope, element, attrs) {
        scope.localization = localization
      }
    }
  }])
}