import { getBearerToken, setBearerToken } from '../util';

function shouldRerouteRequest(config) {
  var decodedUrl = decodeURIComponent(window.location.href);
  var pageReachedBySecureLink = (decodedUrl.includes('hakutoiveidenMuokkaus.html') || decodedUrl.includes('token'));
  return pageReachedBySecureLink && config.url.includes('/secure/')
}

function shouldAuthenticate(config) {
  return config.url.includes('insecure/')
}

export default ['$injector', function HakemusInterceptor($injector) {
  return {
    request: function(config) {
      console.log('$injector.get($cookies)[CSRF]: ' + $injector.get('$cookies')['CSRF']);
      if (shouldRerouteRequest(config)) {
        config.url = config.url.replace(/\/secure\//, '/insecure/')
      }
      if (shouldAuthenticate(config) && getBearerToken()) {
        config.headers.Authorization = 'Bearer ' + getBearerToken()
      }
      config.headers.CSRF = $injector.get('$cookies')['CSRF'];
      return config
    },

    response: function(response) {
      if (shouldAuthenticate(response.config) && response.data && response.data.jsonWebToken) {
        setBearerToken(response.data.jsonWebToken);
        response.oiliJwt = response.data.oiliJwt;
        response.data = response.data.response
      }
      return response
    }
  };
}]
