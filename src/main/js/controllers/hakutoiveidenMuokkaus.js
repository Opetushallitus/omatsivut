var Hakemus = require('./hakemuseditori').Hakemus
var util = require('../util')

module.exports = function(app, staticResources) {
  app.controller('HakutoiveidenMuokkausController', function($scope, $location, $http) {
    var matches = $location.path().match(/token\/(.+)/)
    var token = matches && matches[1]
    var baseUrl = 'insecure/applications/application/'

    $scope.lang = staticResources.translations.languageId

    if (token || util.getBearerToken()) {
      $scope.loading = true
      $location.path('/').replace()
      var suffix = token ? 'token/' + token : 'session'
      $http.get(baseUrl + suffix).success(function(response) {
        $scope.loading = false
        $scope.hakemus = new Hakemus(response.hakemusInfo)
        var henkilotiedot = response.hakemusInfo.hakemus.answers.henkilotiedot
        $scope.user = {
          name: henkilotiedot.Kutsumanimi + ' ' + henkilotiedot.Sukunimi
        }
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
