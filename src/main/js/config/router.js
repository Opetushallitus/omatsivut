import HakemusInterceptor from '../interceptors/nonSensitiveHakemus';

export default ['$httpProvider', '$locationProvider', function($httpProvider, $locationProvider) {
  $locationProvider.html5Mode(true);
  $httpProvider.interceptors.push(HakemusInterceptor);
}]
