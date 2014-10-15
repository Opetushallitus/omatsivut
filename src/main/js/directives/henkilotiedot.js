module.exports = function(listApp) {
  listApp.directive("henkilotiedot", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        yhteystiedot: '=data'
      },
      templateUrl: 'templates/henkilotiedot.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization

      }
    }
  }])
}