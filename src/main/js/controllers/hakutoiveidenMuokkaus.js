module.exports = function(app) {
    app.controller('HakutoiveidenMuokkausController', function($scope, $location, $http) {
        var matches = $location.path().match(/token\/(.+)/)
        var token = matches && matches[1]
        var baseUrl = 'insecure/applications/application/'
        var bearerTokenKey = 'bearerToken'

        $scope.lang = 'fi' // Todo

        function getBearerToken() {
            return window.sessionStorage.getItem(bearerTokenKey)
        }

        if (token || getBearerToken()) {
            $scope.loading = true
            $location.path('/').replace()
            var suffix = token ? 'token/' + token : 'session'
            $http.get(baseUrl + suffix, {
                headers: {
                    Authorization: 'Bearer' + getBearerToken()
                }
            }).success(function(response) {
                $scope.loading = false
                $scope.hakemus = response
                window.sessionStorage.setItem(bearerTokenKey, response.jsonWebToken)
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