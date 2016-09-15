var Hakemus = require('./hakemuseditori').Hakemus

module.exports = function(listApp) {
  listApp.directive("applicationList", ["localization", "restResources", function (localization, restResources) {
    return {
      restrict: 'E',
      scope: true,
      templateUrl: 'templates/applicationList.html',

      link: function ($scope, element, attrs) {
        $scope.$on("hakutoive-vastaanotettu", function() {
          loadApplications()
        })
        $scope.loadApplications = loadApplications
        loadApplications()

        function loadApplications() {
          $scope.applicationStatusMessage = "message.loadingApplications"
          $scope.applicationStatusMessageType = "ajax-spinner"
          restResources.applications.query(success, error)
        }

        function success(data) {
          $scope.applications = _.map(data, function(json) { return new Hakemus(json) })
          if($scope.applications.length > 0) {
            $scope.applicationStatusMessage = ""
            $scope.applicationStatusMessageType = ""
          }
          else {
            $scope.applicationStatusMessage = "message.noApplications"
            $scope.applicationStatusMessageType = "info"
          }
        }

        function error(err) {
          switch (err.status) {
            case 401:
              document.location.replace("/omatsivut/login")
              break;
            case 404:
              $scope.applicationStatusMessage = "errorPage.noApplicationsFound.text"
              $scope.applicationStatusMessageType = "info"
              break;
            default:
              $scope.applicationStatusMessage = "error.loadingFailed"
              $scope.applicationStatusMessageType = "error"
          }
          $scope.applications = [];
        }
      }
    }
  }])
}
