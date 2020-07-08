import Cookies from 'js-cookie';
import { urls } from './constants';

export async function init() {
  const language = getLanguage();
  document.documentElement.lang = language;
  try {
    let translations = await loadTranslations(language);
    window.translations = translations;
  } catch (err) {
    console.error(err);
    throw new Error("Error in init(): failed to await loadTranslations: " + err);
  }

  if (window.translations === undefined) {
    throw new Error("Error in init(): loadTranslations() returned undefined. language: " + language);
  }
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
  const headers = new Headers({
    'Caller-Id': '1.2.246.562.10.00000000001.omatsivut.frontend'
  });
  const request = new Request(url, {
    method: 'GET',
    headers: headers
  });
  return fetch(request)
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
