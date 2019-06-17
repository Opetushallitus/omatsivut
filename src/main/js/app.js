import 'babel-polyfill';
import angular from 'angular';
import ngResource from 'angular-resource';
import ngSanitize from 'angular-sanitize';
import ngAnimate from 'angular-animate';
import ngCookies from 'angular-cookies';
import typeahead from 'angular-ui-bootstrap/src/typeahead/index.js';
import '../less/main.less';
import '../less/hakutoiveidenMuokkaus.less';
import '../less/preview.less';

require('./recursionHelper');
require('../lib/angular-debounce');
import { init } from './staticResources';
import { isTestMode } from './util';
// Services
import ApplicationValidator from './services/applicationValidator';
import AngularBacon from './services/angularBacon';
import localize from './localization';
import RestResources from './restResources';
import Settings from './settings';
import router from './config/router';
// Directives
import ApplicationList from './directives/applicationList';
import Notification from './directives/notification';
import Confirm from './directives/confirm';
import Question from './directives/question';
import LocalizedLink from './directives/localizedLink';
import FormattedTime from './directives/formattedTime';
import Sortable from './directives/sortable';
import DisableClickFocus from './directives/disableClickFocus';
import IgnoreDirty from './directives/ignoreDirty';
import Application from './directives/application';
import HakutoiveenVastaanotto from './directives/hakutoiveenVastaanotto';
import Ilmoittautuminen from './directives/ilmoittautuminen';
import Kela from './directives/kela';
import Hakutoiveet from './directives/hakutoiveet';
import Valintatulos from './directives/valintatulos';
import Henkilotiedot from './directives/henkilotiedot';
import ApplicationPeriods from './directives/applicationPeriods';
import ClearableInput from './directives/clearableInput';
import Callout from './directives/callout';
import Lasnaoloilmoittautuminen from '../components/lasnaoloilmoittautuminen/lasnaoloilmoittautuminen';

// Controllers
import HakutoiveidenMuokkausController from './controllers/hakutoiveidenMuokkaus';
import AdditionalQuestionController from './controllers/additionalQuestionController';
import HakutoiveController from './controllers/hakutoiveController';

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

const listApp = angular.module('listApp',
  [ngResource, ngSanitize, ngAnimate, ngCookies, typeahead, "RecursionHelper", "debounce", "exceptionOverride"]);

listApp
  .config(router)
  .factory('restResources', RestResources)
  .factory('angularBacon', AngularBacon)
  .factory('applicationValidator', ApplicationValidator)
  .factory('settings', Settings)
  .directive('applicationList', ApplicationList)
  .directive('notification', Notification)
  .directive('confirm', Confirm)
  .directive('question', Question)
  .directive('localizedLink', LocalizedLink)
  .directive('formattedTime', FormattedTime)
  .directive('sortable', Sortable)
  .directive('disableClickFocus', DisableClickFocus)
  .directive('application', Application)
  .directive('hakutoiveenVastaanotto', HakutoiveenVastaanotto)
  .directive('ilmoittautuminen', Ilmoittautuminen)
  .directive('kela', Kela)
  .directive('hakutoiveet', Hakutoiveet)
  .directive('valintatulos', Valintatulos)
  .directive('henkilotiedot', Henkilotiedot)
  .directive('applicationPeriods', ApplicationPeriods)
  .directive('ignoreDirty', IgnoreDirty)
  .directive('clearableInput', ClearableInput)
  .directive('callout', Callout)
  .directive('lasnaoloilmoittautuminen', Lasnaoloilmoittautuminen)
  .controller('hakutoiveidenMuokkausController', HakutoiveidenMuokkausController)
  .controller('additionalQuestionController', AdditionalQuestionController)
  .controller('hakutoiveController', HakutoiveController);

listApp.run(['$rootScope', function ($rootScope) {
  $rootScope.localization = localize;
}]);

listApp.run(['$http', '$cookies', function($http, $cookies) {
  $http.defaults.headers.common['Caller-Id'] = "1.2.246.562.10.00000000001.omatsivut.frontend";
  if($cookies['CSRF']) {
    $http.defaults.headers.common['CSRF'] = $cookies['CSRF'];
  }
}]);

angular.element(document).ready(
  init()
    .then(() => {
      angular.bootstrap(document, ['listApp']);
      document.getElementsByTagName('body')[0].setAttribute('aria-busy', 'false');
    })
);

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
  logExceptionToPiwik(errorMsg, data);
};

angular.module("exceptionOverride", []).factory("$exceptionHandler", ["$injector", function($injector) {
  var loggedErrors = [];
  var skippedErrors = 0;
  return function (exception, cause) {
    var $http = $injector.get("$http");
    var $window = $injector.get("$window");
    function getBrowserAndVersion() {
      var N = navigator.appName;
      var ua = navigator.userAgent;
      var M = ua.match(/(opera|chrome|safari|firefox|msie)\/?\s*(\.?\d+(\.\d+)*)/i);
      M = M ? [
        M[1],
        M[2]
      ] : [
        N,
        navigator.appVersion,
        '-?'
      ];
      return [M[0],M[1]];
    }
    function logToBackend(data, errorId) {
      loggedErrors.push(errorId);
      $http.post(window.url("omatsivut.errorlogtobackend"), JSON.stringify(data))
        .then(function(success){
            console.log("Error successfully logged to backend, " + success.status);
          },
          function(failure) {
            console.log("Backend call for error logging failed, ", failure.status);
          });
    }
    try {

      var browser = '';
      var browserVersion = '';
      try {
        var bv = getBrowserAndVersion();
        browser = bv[0];
        browserVersion = bv[1];
      } catch (e) {
        console.log("Something went wrong in deducing browser or browser version: ", e);
      }
      var errorMessage = '';
      var stackTrace = '';
      if (exception !== undefined) {
        errorMessage = exception.toString();
        if(exception.stack !== undefined) {
          stackTrace = exception.stack.toString();
        }
      }
      var errorInfo = {
        errorUrl: $window.location.href,
        errorMessage: errorMessage,
        stackTrace: stackTrace,
        cause: cause || '',
        browser: browser,
        browserVersion: browserVersion
      };
      var errorId = $window.location.href + '---' + errorMessage;
      if (loggedErrors.indexOf(errorId) !== -1) {
        console.log("Error with id has already been logged, aborting! ", errorId);
        skippedErrors += 1;
        console.log("skipped errors: ", skippedErrors );
        // älä lähetä, jos jo lokitettu tai lokitus keskeytetty
        return;
      } else {
        if (isTestMode()) {
          logToBackend(errorInfo, errorId);
        } else {
          logToBackend(errorInfo, errorId);
          logExceptionToPiwik(exception.message, exception.stack);
        }
      }
    } catch (e) {
        console.log("Error while sending error data to backend: ", e.toString())
    }
  };
}]);
