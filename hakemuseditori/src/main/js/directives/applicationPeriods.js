var util = require("../util")

module.exports = function(app) {
  app.directive("applicationPeriods", ["localization", function (localization) {
    return {
      restrict: 'E',
      scope: {
        haku: '&haku'
      },
      templateUrl: 'templates/applicationPeriods.html',
      link: function ($scope, element, attrs) {
        $scope.localization = localization
        $scope.periods = function() { return $scope.haku().applicationPeriods }

        $scope.applicationPeriodString = function(index) {
          if ($scope.periods().length === 1)
            return localization("label.applicationPeriod")
          else
            return (index+1) + ". " + localization("label.applicationPeriod").toLowerCase()
        }

        $scope.statusString = function(period) {
          if (period.active)
            return localization("label.applicationPeriodActive")
          else if (period.end < new Date().getTime())
            return localization("label.applicationPeriodPassed")
          else
            return localization("label.applicationPeriodNotStarted")
        }
      }
    }
  }])
}