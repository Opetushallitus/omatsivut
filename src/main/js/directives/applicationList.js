const _ = require('underscore');
const Hakemus = require('../hakemuseditori/hakemuseditori').Hakemus;

export default function(listApp) {
  listApp.directive("applicationList", ["restResources", function (restResources) {
    return {
      restrict: 'E',
      scope: true,
      template: require('./applicationList.html'),

      link: function ($scope, element, attrs) {
        $scope.$on("hakutoive-vastaanotettu", function() {
          loadApplications()
        });
        $scope.loadApplications = loadApplications
        loadApplications();

        function loadApplications() {
          $scope.applicationStatusMessage = "message.loadingApplications";
          $scope.applicationStatusMessageType = "ajax-spinner";
          restResources.applications.get(success, error)
        }

        function success(data) {
          $scope.allApplicationsFetched = data.allApplicationsFetched;
          $scope.applications = _.map(data.applications, function(json) { return new Hakemus(json) });
          if($scope.applications.length > 0) {
            $scope.applicationStatusMessage = "";
            $scope.applicationStatusMessageType = ""
          } else if (!$scope.allApplicationsFetched) {
            $scope.applicationStatusMessage = "error.loadingFailed";
            $scope.applicationStatusMessageType = "error"
          } else {
            $scope.applicationStatusMessage = "message.noApplications";
            $scope.applicationStatusMessageType = "info"
          }
        }

        function error(err) {
          switch (err.status) {
            case 401:
              document.location.replace(window.url("omatsivut.login"));
              break;
            case 404:
              $scope.applicationStatusMessage = "errorPage.noApplicationsFound.text";
              $scope.applicationStatusMessageType = "info";
              break;
            default:
              $scope.applicationStatusMessage = "error.loadingFailed";
              $scope.applicationStatusMessageType = "error"
          }
          $scope.applications = [];
        }
      }
    }
  }])
};
