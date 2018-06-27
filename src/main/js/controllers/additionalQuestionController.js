import localize from '../localization';

export default ["$scope", function($scope) {
  $scope.localization = localize;
  $scope.questionAnswered = function() {
    $scope.$emit("questionAnswered")
  }
}]
