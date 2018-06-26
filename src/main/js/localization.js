import { getTranslations }  from './staticResources';

function getValue(obj, path) {
  const parts = path.split(".");
  return parts.reduce((memo, val) => {
    return memo == null ? null : memo[val];
  }, obj)
}

function replaceVars(value, vars) {
  const NON_BREAKING_SPACE = "\u00A0";
  return vars.reduce((memo, val, key) => {
    return memo.replace("__" + key + "__", val)
  }, value).replace(/_/g, NON_BREAKING_SPACE)
}

export default function localize(key, vars) {
  const val = getValue(getTranslations(), key);
  if (!val) {
    throw new Error("no translation for " + key);
  } else {
    return replaceVars(val, vars || {});
  }
}
