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
  var language = "fi"
  var self = this
  $.ajax({ url: "/omatsivut/translations/" + language + ".json", dataType: "json" }).done(function(data) {
    callback(data)
  })
}