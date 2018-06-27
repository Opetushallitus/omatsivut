import localize from '../localization';

export default function () {
  return {
    restrict: 'E',
    scope: {
      haku: '&haku'
    },
    template: require('./applicationPeriods.html'),
    link: function ($scope, element, attrs) {
      $scope.localization = localize;
      $scope.periods = function() { return $scope.haku().applicationPeriods }

      $scope.applicationPeriodString = function(index) {
        if ($scope.periods().length === 1)
          return localize("label.applicationPeriod")
        else
          return (index+1) + ". " + localize("label.applicationPeriod").toLowerCase()
      }

      $scope.statusString = function(period) {
        if (period.active)
          return localize("label.applicationPeriodActive")
        else if (period.end < new Date().getTime())
          return localize("label.applicationPeriodPassed")
        else
          return localize("label.applicationPeriodNotStarted")
      }
    }
  }
}
