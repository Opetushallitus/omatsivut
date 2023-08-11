import { getBearerToken, setBearerToken } from '../util';

function shouldRerouteRequest(config) {
  var decodedUrl = decodeURIComponent(window.location.href);
  var pageReachedBySecureLink = (decodedUrl.includes('hakutoiveidenMuokkaus.html') || decodedUrl.includes('token'));
  return pageReachedBySecureLink && config.url.includes('/secure/')
}

function shouldAuthenticate(config) {
  return config.url.includes('insecure/')
}

export default ['$cookies', function HakemusInterceptor($cookies) {
  return {
    request: function(config) {
      if (shouldRerouteRequest(config)) {
        config.url = config.url.replace(/\/secure\//, '/insecure/')
      }
      const bearerToken = getBearerToken($cookies);
      if (shouldAuthenticate(config) && bearerToken) {
        config.headers.Authorization = 'Bearer ' + bearerToken;
      }
      config.headers.CSRF = $cookies.get('CSRF');
      return config
    },

    response: function(response) {
      if (shouldAuthenticate(response.config) && response.data && response.data.jsonWebToken) {
        setBearerToken($cookies, response.data.jsonWebToken);
        response.oiliJwt = response.data.oiliJwt;
        response.data = response.data.response
      }
      return response
    }
  };
}]
