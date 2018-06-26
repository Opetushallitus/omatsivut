import 'babel-polyfill';
require("angular");
require('ng-resource')(window, angular);
require('angular-module-sanitize');
require('angular-animate');
require('angular-cookies');
require("../lib/ui-bootstrap-custom-tpls-0.10.0.min.js");
require('./recursionHelper');
require('../lib/angular-debounce');
import { init } from './staticResources';
import { isTestMode } from './util';
import localize from './localization';
import RestResources from './restResources';
import Settings from './settings';
import moment from './moment';
window.moment = moment;

require("../lib/oph_urls.js/index.js");
require("./omatsivut-web-oph.js");

window.Service = {
  login: function() {
    document.location.href = "/omatsivut/login";
  },
  logout: function() {
    document.location.href = "/omatsivut/logout";
  },
  getUser: function() {
    return new Promise((resolve, reject) => {
      fetch('/omatsivut/session', {
        credentials: 'same-origin'
      })
      .then((response) => {
        if (response.status === 200) {
          response.json().then((user) => {
            resolve(user);
          })
        } else {
          reject(new Error('No session found!'));
        }
      }).catch(err => {
        console.error(err);
        reject(new Error('Failed to fetch session!'));
      });
    });
  }
};
// "exceptionOverride"
const listApp = angular.module('listApp', ["ngResource", "ngSanitize", "ngAnimate", "ngCookies", "RecursionHelper", "ui.bootstrap.typeahead", "template/typeahead/typeahead-popup.html", "template/typeahead/typeahead-match.html", "debounce"], function($locationProvider) {
  $locationProvider.html5Mode(false)
});

listApp
  .config()
  .service('restResources', RestResources)
  .service('settings', Settings);

/*
require('./hakemuseditori/hakemuseditori')(listApp);
require('./directives/applicationList')(listApp);
require('./directives/notification')(listApp);
require('./controllers/hakutoiveidenMuokkaus')(listApp);
*/

listApp.config(function ($httpProvider) {
  $httpProvider.interceptors.push(require('./interceptors/nonSensitiveHakemus'))
});

listApp.run(function ($rootScope) {
  $rootScope.localization = localize;
});

listApp.run(function($http, $cookies) {
  $http.defaults.headers.common['clientSubSystemCode'] = "omatsivut.frontend";
  if($cookies['CSRF']) {
    $http.defaults.headers.common['CSRF'] = $cookies['CSRF'];
  }
});

angular.element(document).ready(() => {
  init(() => {
    angular.bootstrap(document, ['listApp']);
    document.getElementsByTagName('body')[0].setAttribute('aria-busy', 'false');
  });
});
/*
function logExceptionToPiwik(msg, data) {
  if (typeof _paq === 'undefined' || _paq == null) {
    console.warn("Piwik not present, cannot log: " + msg + "\n" + data)
  } else {
    console.warn(msg + "\n" + data);
    _paq.push(["trackEvent", document.location, msg, data])
  }
}

window.onerror = function(errorMsg, url, lineNumber, columnNumber, exception) {
  let data = url + ":" + lineNumber;
  if (typeof columnNumber !== "undefined") data += ":" + columnNumber;
  if (typeof exception !==  "undefined") data += "\n" + exception.stack;
  logExceptionToPiwik(errorMsg, data)
};

angular.module("exceptionOverride", []).factory("$exceptionHandler", function() {
  return function (exception) {
    if (isTestMode()) {
      throw exception
    } else {
      logExceptionToPiwik(exception.message, exception.stack)
    }
  };
});
*/
