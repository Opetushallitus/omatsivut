module.exports = {
  mapArray: function(array, keyField, valueField) {
    return _.reduce(array, function(memo, item) {
      var key = item[keyField]
      if (memo[key] == null)
        memo[key] = []
      memo[key].push(item[valueField])
      return memo
    }, {});
  },

  flattenTree: function(rootNode, childrenAttribute) {
    return (function flatten(node, list) {
      if (node != null) {
        if (node[childrenAttribute] == null)
          list.push(node)
        else
          _(node[childrenAttribute]).each(function(subnode) { flatten(subnode, list) })
      }
      return list
    })(rootNode, [])
  }
}