const BEARER_TOKEN_KEY = 'bearerToken';

export default {
  getBearerToken: function() {
    return window.sessionStorage.getItem(BEARER_TOKEN_KEY)
  },
  setBearerToken: function(token) {
    window.sessionStorage.setItem(BEARER_TOKEN_KEY, token)
  },
  removeBearerToken: function() {
    window.sessionStorage.removeItem(BEARER_TOKEN_KEY)
  }
};

export function isTestMode() {
  return window.parent.location.href.indexOf("runner.html") > 0;
}
