var Hakemus = require('./hakemus')
module.exports = function(listApp) {
  listApp.controller("listController", ["$scope", "restResources", "localization", function ($scope, restResources, localization) {
    $scope.applicationStatusMessage = localization("message.loadingApplications")
    $scope.applicationStatusMessageType = "ajax-spinner";
    restResources.applications.query(success, error)

    function success(data) {
      $scope.applications = _.map(data, function(json) { return new Hakemus(json) })
      if($scope.applications.length > 0) {
        $scope.applicationStatusMessage = "";
        $scope.applicationStatusMessageType = "";
      }
      else {
        $scope.applicationStatusMessage = localization("message.noApplications");
        $scope.applicationStatusMessageType = "info";
      }
    }

    function error(err) {
      switch (err.status) {
        case 401: $scope.applicationStatusMessage = localization("error.loadingFailed_notLoggedIn"); break;
        default: $scope.applicationStatusMessage = localization("error.loadingFailed");
      }
      $scope.applicationStatusMessageType = "error"
      $scope.applications = [];
    }
  }]);
}