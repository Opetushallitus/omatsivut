import localize from '../localization';
const _ = require('underscore');

export default function () {
  return {
    restrict: 'E',
    scope: {
      application: "=",
      preferenceMoved: "=",
      validating: "="
    },
    template: require('./hakutoiveet.html'),

    link: function ($scope, element, attrs) {
      $scope.localization = localize;

      $scope.movePreference = function(from, to) {
        if (to >= 0 && to < this.application.hakutoiveet.length) {
          this.application.movePreference(from, to)
          this.preferenceMoved()
        }
      }
    }
  }
}
