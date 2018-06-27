import { getLanguage } from '../staticResources';
import { getBearerToken, removeBearerToken } from '../util';
import Hakemus from '../models/hakemus';

export default ['$scope', '$location', '$http', function($scope, $location, $http) {
  const matches = $location.path().match(/token\/(.+)/);
  const token = matches && matches[1];
  const baseUrl = 'insecure/applications/application/';

  // Ladataan sivu sen jälkeen kun vastaanottotieto on lähetetty
  $scope.$on("hakutoive-vastaanotettu", function() {
    location.reload();
  });

  $scope.lang = getLanguage();

  $scope.logout = function() {
    removeBearerToken();
    $scope.application = null;
    $scope.loggedOut = true;
  };

  if (token || getBearerToken()) {
    $scope.loading = true;
    $location.path('/').replace();
    const suffix = token ? 'token/' + token : 'session';
    $http.get(baseUrl + suffix).then(
      function (response) {
        $scope.loading = false;
        $scope.application = new Hakemus(response.data);
        $scope.application.oiliJwt = response.oiliJwt;
        const henkilotiedot = response.data.hakemus.answers.henkilotiedot;
        $scope.allowVastaanotto = !henkilotiedot.Henkilotunnus;
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
}]
