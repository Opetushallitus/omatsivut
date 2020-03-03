import HakemusInterceptor from '../interceptors/nonSensitiveHakemus';
import RestErrorInterceptor from '../interceptors/restError';

export default ['$httpProvider', '$locationProvider', function($httpProvider, $locationProvider) {
  $locationProvider.html5Mode(true);
  $httpProvider.interceptors.push(HakemusInterceptor);
  $httpProvider.interceptors.push(RestErrorInterceptor);
}]
