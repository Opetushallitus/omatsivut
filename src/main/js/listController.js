var Hakemus = require('./hakemus')
module.exports = function(listApp) {
  listApp.controller("listController", ["$scope", "applicationsResource", "localization", function ($scope, applicationsResource, localization) {
    $scope.applicationStatusMessage = localization("message.loadingApplications")
    $scope.applicationStatusMessageType = "ajax-spinner";
    applicationsResource.query(success, error)

    function success(data) {
      $scope.applications = _.map(data, function(json) { return new Hakemus(json) })
      if($scope.applications.length > 0) {
        $scope.applicationStatusMessage = "";
        $scope.applicationStatusMessageType = "";
      }
      else {
        $scope.applicationStatusMessage = localization("noApplications");
        $scope.applicationStatusMessageType = "info";
      }
    }

    function error(err) {
      switch (err.status) {
        case 401: $scope.applicationStatusMessage = localization("error.loadingFailed.notLoggedIn"); break;
        default: $scope.applicationStatusMessage = localization("error.loadingFailed");
      }
      $scope.applicationStatusMessageType = "error"
      $scope.applications = [];
    }
  }]);
}