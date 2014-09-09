module.exports = function(listApp, resources) {
  listApp.factory("localization", [function() {
    function getValue(obj, path) {
      var parts = path.split(".")
      return _.reduce(parts, function(memo, val) {
        return memo == null ? null : memo[val]
      }, obj)
    }

    function replaceVars(value, vars) {
      return _.reduce(vars, function(memo, val, key) {
        return memo.replace("__" + key + "__", val)
      }, value)
    }

    return function(key, vars) {
      var val = getValue(resources.translations, key)
      if (!val)
        throw new Error("no translation for " + key)
      else
        return replaceVars(val, vars || {})
    }
  }])
}