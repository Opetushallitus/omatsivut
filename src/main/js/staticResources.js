const _ = require('underscore');
import Cookies from 'js-cookie';

window.translations;

export function init(callback) {
  const language = getLanguage();
  setTimeformat(language);
  loadTranslations(language, function(translations) {
    window.translations = translations;
  });
  callback();
}

export function getLanguage() {
  let lang = Cookies.get('lang');
  if (lang) {
    return lang;
  }

  return getLanguageFromHost();
}

export function getTranslations() {
  return window.translations;
}

function setTimeformat(language) {
  if (language === "en")
    window.moment.locale("en-gb");
  else
    window.moment.locale(language)
}

function loadTranslations(language, callback) {
  $('html').attr('lang', language);
  fetch(window.url("omatsivut.translations"))
    .then(response => response.json())
    .then(translations => callback(translations))
    .catch(err => console.error(err));
}

function getLanguageFromHost(host) {
  if (!host) { host = document.location.host; }

  let parts = host.split('.');
  if (parts.length < 2) {
    return 'fi';
  }

  let domain = parts[parts.length - 2];
  if (domain.indexOf('opintopolku') > -1) {
    return 'fi';
  } else if (domain.indexOf('studieinfo') > -1) {
    return 'sv';
  } else if (domain.indexOf('studyinfo') > -1) {
    return 'en'
  }
  return 'fi'
}
