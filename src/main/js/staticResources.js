var resources = {
  init: function(callback) {
    loadTranslations(function(translations) {
      resources.translations = translations
      callback()
    })
  }
}

module.exports = resources

function loadTranslations(callback) {
  var language = readLanguageCookie()
  $('html').attr('lang', language)
  var self = this
  $.ajax({ url: "/omatsivut/translations/" + language + ".json", dataType: "json" }).done(function(data) {
    callback(data)
  })
}

function readLanguageCookie() {
    var cname = encodeURIComponent("i18next")
    if(document.cookie.length > 0) {
        var cookies = document.cookie.split(/; */);
        var cookie = _.chain(cookies)
            .map(function (c) { return c.split('=') })
            .find(function (val) { return val[0] == cname })
            .value()
        if (cookie) {
            return decodeURIComponent(cookie[1])
        }
    }
    return "fi"
}
