import { getBearerToken, setBearerToken } from '../util';

function shouldRerouteRequest(config) {
  return window.location.href.includes('hakutoiveidenMuokkaus.html') && config.url.includes('/secure/')
}

function shouldAuthenticate(config) {
  return config.url.includes('insecure/')
}

export default function HakemusInterceptor() {
  return {
    request: function(config) {
      if (shouldRerouteRequest(config)) {
        config.url = config.url.replace(/\/secure\//, '/insecure/')
      }
      if (shouldAuthenticate(config) && getBearerToken()) {
        config.headers.Authorization = 'Bearer ' + getBearerToken()
      }
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
}
