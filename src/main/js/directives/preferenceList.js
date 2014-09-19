module.exports = function(listApp) {
  listApp.directive("preferenceList", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        application: "=application",
        preferenceMoved: "=preferenceMoved"
      },
      templateUrl: 'templates/preferenceList.html',

      link: function ($scope, element, attrs) {
        $scope.localization = localization

        $scope.movePreference = function(from, to) {
          if (to >= 0 && to < this.application.hakutoiveet.length) {
            this.application.movePreference(from, to)
            this.preferenceMoved()
          }
        }
      }
    }
  }])
}