import HakemusInterceptor from '../interceptors/nonSensitiveHakemus';
import RestErrorInterceptor from '../interceptors/restError';

export default ['$httpProvider', '$locationProvider', '$cookies', function($httpProvider, $locationProvider, $cookies) {
  $locationProvider.html5Mode(true);
  $httpProvider.interceptors.push(HakemusInterceptor($cookies));
  $httpProvider.interceptors.push(RestErrorInterceptor);
}]
