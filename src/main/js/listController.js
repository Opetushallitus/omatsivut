var Hakemus = require('./hakemus')
module.exports = function(listApp) {
  listApp.controller("listController", ["$scope", "applicationsResource", function ($scope, applicationsResource) {
    $scope.applicationStatusMessage = "Hakemuksia ladataan...";
    $scope.applicationStatusMessageType = "ajax-spinner";
    applicationsResource.query(success, error)

    function success(data) {
      $scope.applications = _.map(data, function(json) { return new Hakemus(json) })
      $scope.applicationStatusMessage = "";
      $scope.applicationStatusMessageType = "";
    }

    function error(err) {
      switch (err.status) {
        case 401: $scope.applicationStatusMessage = "Tietojen lataus epäonnistui: et ole kirjautunut sisään."; break;
        default: $scope.applicationStatusMessage = "Tietojen lataus epäonnistui. Yritä myöhemmin uudelleen.";
      }
      $scope.applicationStatusMessageType = "error"
      $scope.applications = [];
    }
  }]);
}