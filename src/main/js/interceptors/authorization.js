var util = require('../util')

function shouldIntercept(config) {
  return config.url.indexOf('insecure/') !== -1
}

var authorizationInterceptor = {
  request: function(config) {
    if (shouldIntercept(config) && util.getBearerToken()) {
      config.headers.Authorization = 'Bearer ' + util.getBearerToken()
    }
    return config
  },

  response: function(response) {
    if (shouldIntercept(response.config) && response.data && response.data.jsonWebToken) {
      util.setBearerToken(response.data.jsonWebToken)
    }
    return response
  }
}

module.exports = function() {
  return authorizationInterceptor
}