import { getTranslations, getLanguage } from './staticResources';
const _ = require('underscore');

function getValue(obj, path) {
  const parts = path.split(".");
  return parts.reduce((memo, val) => {
    return memo == null ? null : memo[val];
  }, obj)
}

function replaceVars(value, vars) {
  const NON_BREAKING_SPACE = "\u00A0";
  return _.reduce(vars, (memo, val, key) => {
    return memo.replace("__" + key + "__", val)
  }, value).replace(/_/g, NON_BREAKING_SPACE)
}

export default function localize(key, vars) {
  const translations = getTranslations();
  const val = getValue(translations, key);
  if (!val) {
    throw new Error("no translation for " + key + ", language: " + getLanguage() + ", translations: " + JSON.stringify(translations));
  } else {
    return replaceVars(val, vars || {});
  }
}
