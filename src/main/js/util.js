var BEARER_TOKEN_KEY = 'bearerToken'

module.exports = {
  getBearerToken: function() {
    return window.sessionStorage.getItem(BEARER_TOKEN_KEY)
  },
  setBearerToken: function(token) {
    window.sessionStorage.setItem(BEARER_TOKEN_KEY, token)
  }
}