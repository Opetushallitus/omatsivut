import HakemusInterceptor from '../interceptors/nonSensitiveHakemus';
import RestErrorInterceptor from '../interceptors/restError';
import { isTestMode } from '../util';

export default ['$httpProvider', '$locationProvider', function($httpProvider, $locationProvider) {
  $locationProvider.html5Mode(true);
  $httpProvider.interceptors.push(HakemusInterceptor);
  if (!isTestMode()) { //Causes a lot of errors in tests as instances get started and killed all the time, non-trivial to fix
    $httpProvider.interceptors.push(RestErrorInterceptor);
  }
}]
