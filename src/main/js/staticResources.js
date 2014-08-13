var resources = {
  init: function(callback) {
    var language = readLanguageCookie()
    setTimeformat(language)
    loadTranslations(language, function(translations) {
      resources.translations = translations
      resources.translations.languageId = language
      callback()
    })
  }
}

module.exports = resources

function setTimeformat(language) {
  if (language == "en")
    moment.locale("en-gb")
  else
    moment.locale(language)
}

function loadTranslations(language, callback) {
  $('html').attr('lang', language)
  var self = this
  $.ajax({ url: "/omatsivut/translations", dataType: "json" }).done(function(data) {
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
            return decodeURIComponent(cookie[1].split("-")[0])
        }
    }
    return "fi"
}
