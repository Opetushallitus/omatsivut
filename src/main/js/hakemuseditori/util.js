const _ = require('underscore');

const utils = {
  mapArray: function (array, keyField, valueField) {
    return _.reduce(array, function (memo, item) {
      var key = item[keyField]
      if (memo[key] == null)
        memo[key] = []
      memo[key].push(item[valueField])
      return memo
    }, {});
  },

  indexBy: function (array, keyFunction) {
    return _.reduce(array, function (memo, item, index) {
      memo[keyFunction(item, index)] = item
      return memo
    }, {})
  },

  flattenTree: function (rootNode, childrenAttribute) {
    return (function flatten(node, list) {
      if (node != null) {
        if (node[childrenAttribute] == null)
          list.push(node)
        else
          _(node[childrenAttribute]).each(function (subnode) {
            flatten(subnode, list)
          })
      }
      return list
    })(rootNode, [])
  },

  underscoreToCamelCase: function (str) {
    return str.toLowerCase().replace(/^(.)|_(.)/g, function (match, char1, char2) {
      return (char1 ? char1 : "" + char2 ? char2 : "").toUpperCase()
    })
  },

  withoutAngularFields: function (obj) {
    if (_.isArray(obj)) {
      return _(obj).map(utils.withoutAngularFields)
    } else if (_.isObject(obj)) {
      return _(obj).reduce(function (memo, val, key) {
        if (key.indexOf("$$") < 0)
          memo[key] = utils.withoutAngularFields(val)
        return memo
      }, {})
    } else
      return obj
  }
};

export default utils;
