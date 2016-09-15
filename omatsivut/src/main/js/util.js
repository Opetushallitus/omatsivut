var BEARER_TOKEN_KEY = 'bearerToken'

module.exports = {
  getBearerToken: function() {
    return window.sessionStorage.getItem(BEARER_TOKEN_KEY)
  },
  setBearerToken: function(token) {
    window.sessionStorage.setItem(BEARER_TOKEN_KEY, token)
  },
  removeBearerToken: function() {
    window.sessionStorage.removeItem(BEARER_TOKEN_KEY)
  }
}