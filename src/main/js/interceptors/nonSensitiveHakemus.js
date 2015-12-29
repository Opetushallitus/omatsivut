function shouldIntercept() {
  return window.location.href.indexOf('hakutoiveidenMuokkaus.html') !== -1
}

var nonSensitiveHakemusInterceptor = {
  request: function(config) {
    if (shouldIntercept()) {
      config.url = config.url.replace(/\/secure\//, '/insecure/')
    }
    return config
  }
}

module.exports = function() {
  return nonSensitiveHakemusInterceptor
}