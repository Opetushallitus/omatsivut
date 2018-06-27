import HakemusInterceptor from '../interceptors/nonSensitiveHakemus';

export default function router($httpProvider, $locationProvider) {
  $locationProvider.html5Mode(true);
  $httpProvider.interceptors.push(HakemusInterceptor);
}

router.$inject = ['$httpProvider', '$locationProvider'];
