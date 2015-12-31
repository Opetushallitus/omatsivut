var util = require('../util')

function shouldRerouteRequest(config) {
  return window.location.href.indexOf('hakutoiveidenMuokkaus.html') !== -1 && config.url.indexOf('/secure/') !== -1
}

function shouldAuthenticate(config) {
  return config.url.indexOf('/insecure/') !== -1
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
      util.setBearerToken(response.data.jsonWebToken)
      response.data = response.data.response
    }
    return response
  }
}

module.exports = function() {
  return nonSensitiveHakemusInterceptor
}
