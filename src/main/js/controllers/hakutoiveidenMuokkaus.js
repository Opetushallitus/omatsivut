var Hakemus = require('../hakemuseditori/hakemuseditori').Hakemus;
var util = require('../util');

module.exports = function(app, staticResources) {
  app.controller('HakutoiveidenMuokkausController', function($scope, $location, $http) {
    var matches = $location.path().match(/token\/(.+)/);
    var token = matches && matches[1];
    var baseUrl = 'insecure/applications/application/';

    $scope.lang = staticResources.translations.languageId;

    $scope.logout = function() {
      util.removeBearerToken();
      $scope.application = null;
      $scope.loggedOut = true;
    };

    if (token || util.getBearerToken()) {
      $scope.loading = true;
      $location.path('/').replace();
      var suffix = token ? 'token/' + token : 'session';
      $http.get(baseUrl + suffix).then(
          function (response) {
            $scope.loading = false;
            $scope.application = new Hakemus(response.data);
            $scope.application.oiliJwt = response.oiliJwt;
            var henkilotiedot = response.data.hakemus.answers.henkilotiedot;
            if(henkilotiedot.Henkilotunnus) {
              $scope.allowVastaanotto = false
            } else {
              $scope.allowVastaanotto = true
            }
            $scope.user = {
              name: henkilotiedot.Kutsumanimi + ' ' + henkilotiedot.Sukunimi
            }
          },
          function (response) {
            $scope.loading = false;
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
};
