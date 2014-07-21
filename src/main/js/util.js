module.exports = {
  mapArray: function(array, keyField, valueField) {
    return _.reduce(array, function(memo, item) {
      var key = item[keyField]
      if (memo[key] == null)
        memo[key] = []
      memo[key].push(item[valueField])
      return memo
    }, {});
  }
}