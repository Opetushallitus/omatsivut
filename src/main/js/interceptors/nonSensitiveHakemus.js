var util = require('../util');

function shouldRerouteRequest(config) {
  return window.location.href.includes('hakutoiveidenMuokkaus.html') && config.url.includes('/secure/')
}

function shouldAuthenticate(config) {
  return config.url.includes('insecure/')
}

var nonSensitiveHakemusInterceptor = {
  request: function(config) {
    if (shouldRerouteRequest(config)) {
      config.url = config.url.replace(/\/secure\//, '/insecure/')
    }
    if (shouldAuthenticate(config) && util.getBearerToken()) {
      config.headers.Authorization = 'Bearer ' + util.getBearerToken()
    }
    return config
  },

  response: function(response) {
    if (shouldAuthenticate(response.config) && response.data && response.data.jsonWebToken) {
      util.setBearerToken(response.data.jsonWebToken);
      response.oiliJwt = response.data.oiliJwt;
      response.data = response.data.response
    }
    return response
  }
};

module.exports = function() {
  return nonSensitiveHakemusInterceptor
};
