module.exports = function(app) {
  app.directive("kela", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        kela: '&kela'
      },
      template: require('./kela.html'),
      link: function (scope, element, attrs) {
        scope.localization = localization
      }
    }
  }])
}
