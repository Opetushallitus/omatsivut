var Hakemus = require('./hakemuseditori').Hakemus
var util = require('../util')

module.exports = function(app, staticResources) {
  app.controller('HakutoiveidenMuokkausController', function($scope, $location, $http) {
    var matches = $location.path().match(/token\/(.+)/)
    var token = matches && matches[1]
    var baseUrl = 'insecure/applications/application/'

    $scope.lang = staticResources.translations.languageId

    $scope.logout = function() {
      util.removeBearerToken()
      $scope.hakemus = null;
      $scope.loggedOut = true;
    }

    if (token || util.getBearerToken()) {
      $scope.loading = true
      $location.path('/').replace()
      var suffix = token ? 'token/' + token : 'session'
      $http.get(baseUrl + suffix).then(
          function (response) {
            $scope.loading = false
            $scope.hakemus = new Hakemus(response.data)
            var henkilotiedot = response.data.hakemus.answers.henkilotiedot
            $scope.user = {
              name: henkilotiedot.Kutsumanimi + ' ' + henkilotiedot.Sukunimi
            }
          },
          function (response) {
            $scope.loading = false
            if (404 === response.status) {
              $scope.errorMessage = 'error.noActiveApplication'
            } else if (response.data && response.data.error === 'expiredToken') {
              $scope.infoMessage = 'info.expiredToken'
            } else if (401 === response.status || 403 === response.status) {
              $scope.errorMessage = 'error.invalidToken'
            } else {
              $scope.errorMessage = 'error.serverError'
            }
          })
    } else {
      $scope.errorMessage = 'error.noTokenAvailable'
    }
  })
}
