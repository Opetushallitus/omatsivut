import HakemusInterceptor from '../interceptors/nonSensitiveHakemus';
import RestErrorInterceptor from '../interceptors/restError';

export default ['$httpProvider', '$locationProvider', '$cookies', function($httpProvider, $locationProvider, $cookies) {
  $locationProvider.html5Mode(true);
  $httpProvider.defaults.headers.post = {
    'CSRF': $cookies['CSRF']
  };
  $httpProvider.defaults.headers.put = {
    'CSRF': $cookies['CSRF']
  };
  $httpProvider.interceptors.push(HakemusInterceptor);
  $httpProvider.interceptors.push(RestErrorInterceptor);
}]
