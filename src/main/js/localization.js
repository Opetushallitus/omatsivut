module.exports = function(listApp, resources) {
  listApp.factory("localization", [function() {
    function getValue(obj, path) {
      var parts = path.split(".")
      return _.reduce(parts, function(memo, val) {
        return memo == null ? null : memo[val]
      }, obj)
    }

    return function(key) {
      var val = getValue(resources.translations, key)
      if (!val)
        throw new Error("no translation for " + key)
      else
        return val
    }
  }])
}