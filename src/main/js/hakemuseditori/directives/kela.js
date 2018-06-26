import localize from '../../localization';

export default function(app) {
  app.directive("kela", [function () {
    return {
      restrict: 'E',
      scope: {
        kela: '&kela'
      },
      template: require('./kela.html'),
      link: function (scope, element, attrs) {
        scope.localization = localize;
      }
    }
  }])
}
