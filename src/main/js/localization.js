module.exports = function(listApp, resources) {
  listApp.factory("localization", [function() {
    return function(key) {
      if (!resources.translations[key])
        throw new Error("no translation for " + key)
      else
        return resources.translations[key]
    }
  }])
}