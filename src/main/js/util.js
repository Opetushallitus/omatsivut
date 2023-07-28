const BEARER_TOKEN_KEY = 'bearerToken';
const _ = require('underscore');

export function mapArray(array, keyField, valueField) {
  return _.reduce(array, function (memo, item) {
    var key = item[keyField]
    if (memo[key] == null)
      memo[key] = []
    memo[key].push(item[valueField])
    return memo
  }, {});
}

export function indexBy(array, keyFunction) {
  return _.reduce(array, function (memo, item, index) {
    memo[keyFunction(item, index)] = item
    return memo
  }, {})
}

export function flattenTree(rootNode, childrenAttribute) {
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
}

export function underscoreToCamelCase(str) {
  return str.toLowerCase().replace(/^(.)|_(.)/g, function (match, char1, char2) {
    return (char1 ? char1 : "" + char2 ? char2 : "").toUpperCase()
  })
}

export function withoutAngularFields(obj) {
  if (_.isArray(obj)) {
    return _(obj).map(withoutAngularFields)
  } else if (_.isObject(obj)) {
    return _(obj).reduce(function (memo, val, key) {
      if (key.indexOf("$$") < 0)
        memo[key] = withoutAngularFields(val)
      return memo
    }, {})
  } else
    return obj
}

export function getBearerToken($cookies) {
  return $cookies.get(BEARER_TOKEN_KEY);
}
export function setBearerToken($cookies, token) {
  $cookies.put(BEARER_TOKEN_KEY, token, {domain: window.baseUrl, samesite: 'strict'});
}
export function removeBearerToken($cookies) {
  $cookies.remove(BEARER_TOKEN_KEY);
}

export function isTestMode() {
  return window.parent.location.href.indexOf("runner.html") > 0;
}


