; module.exports = function(listApp) {
  listApp.directive("hakutoiveenVastaanotto", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        hakutoiveet: '&hakutoiveet'
      },
      templateUrl: 'templates/hakutoiveenVastaanotto.html',
      link: function (scope, element, attrs) {
        scope.vastaanottotila = ""
        scope.localization = localization
      }
    }
  }])
}