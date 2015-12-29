var Hakemus = require('./hakemuseditori').Hakemus
var util = require('../util')

module.exports = function(app) {
    app.controller('HakutoiveidenMuokkausController', function($scope, $location, $http) {
        var matches = $location.path().match(/token\/(.+)/)
        var token = matches && matches[1]
        var baseUrl = 'insecure/applications/application/'

        $scope.lang = 'fi' // Todo

        if (token || util.getBearerToken()) {
            $scope.loading = true
            $location.path('/').replace()
            var suffix = token ? 'token/' + token : 'session'
            $http.get(baseUrl + suffix).success(function(response) {
                $scope.loading = false
                $scope.hakemusInfo = new Hakemus(response.hakemusInfo)
            }).error(function(response) {
                $scope.loading = false
                $scope.error = angular.extend({}, response)
            })
        } else {
            $scope.error = {
                errorType: 'noTokenAvailable'
            }
        }
    })
}