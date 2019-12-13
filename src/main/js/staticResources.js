import Cookies from 'js-cookie';
import { urls } from './constants';

export async function init() {
  const language = getLanguage();
  document.documentElement.lang = language;
  let translations = await loadTranslations(language);
  if (translations === undefined) {
    throw new Error("Error in init(): loadTranslations() returned undefined. language:" + language);
  }
  window.translations = translations;
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

function loadTranslations(language) {
  const url = urls["omatsivut.translations"] + '?lang=' + language;
  return fetch(url)
    .then(response => {
      if (response.status === 200) {
        return response.json();
      } else {
        throw new Error("Bad response from " + url + ": " + response);
      }
    })
    .then(translations => translations)
    .catch(err => {
        console.error(err);
        throw new Error("Failed to load translations from " + url + ": " + err);
      });
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
