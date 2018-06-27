const _ = require('underscore');

export default ['RecursionHelper', function(RecursionHelper) {
  return {
    restrict: 'E',
    scope: {
      questionNode: '=',
      application: '=',
      level: '='
    },
    template: require('./question.html'),
    compile: function (element) {
      return RecursionHelper.compile(element, function ($scope, iElement, iAttrs, controller, transcludeFn) {
        $scope.isGroup = function () {
          return $scope.questionNode && !_.isEmpty($scope.questionNode.questionNodes)
        }
      })
    }
  }
}]
