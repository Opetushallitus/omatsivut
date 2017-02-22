var Hakemus = require('./hakemuseditori').Hakemus

module.exports = function(listApp) {
  listApp.directive("applicationList", ["localization", "restResources", "$rootScope", function (localization, restResources, $rootScope) {
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
          $rootScope.kelaURL = _.chain(data)
            .map(function(d) {return d.kelaURL;})
            .filter(function(k) {return k != undefined;})
            .head()
            .value()

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
              document.location.replace(window.url("omatsivut.login"))
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
