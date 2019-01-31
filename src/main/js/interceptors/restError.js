//import { getBearerToken, setBearerToken } from '../util';
import { isTestMode } from '../util';

function shouldRerouteRequest(config) {
  var decodedUrl = decodeURIComponent(window.location.href);
  var pageReachedBySecureLink = (decodedUrl.includes('hakutoiveidenMuokkaus.html') || decodedUrl.includes('token'));
  return pageReachedBySecureLink && config.url.includes('/secure/')
}

function shouldAuthenticate(config) {
  return config.url.includes('insecure/')
}

export default ["$injector", function RestErrorInterceptor($injector) {
  var errors = 0;
  var duplicates_skipped = 0;
  var loggedErrors = [];

  return {
    responseError: function (error) {
      var $http = $injector.get("$http");
      var logEndpoint = window.url("omatsivut.errorlogtobackend");
      if (error === undefined) {
        return error;
      }
      console.log("Caught REST error. Errors before this one: " + errors + ", duplicates skipped: " + duplicates_skipped);
      try {
        var failedRequestUrl = (error.config !== undefined && error.config.url !== undefined) ? error.config.url : 'unknown url';
        if (failedRequestUrl.indexOf(logEndpoint) !== -1 || failedRequestUrl === 'unknown url') {
          if (failedRequestUrl === 'unknown url') {
            console.log("Won't log a failed rest request to an unknown url");
          } else {
            console.log("Error came from logging endpoint, won't try to log it to avoid a loop")
          }
        } else {
          console.log("kissa ready to parse error! ", error);
          var errorData = error.data !== undefined ? JSON.stringify(error.data) : '';
          var statusCode = error.status !== undefined ? error.status : '-1';
          var statusText = error.statusText !== undefined ? error.statusText : '';
          var config = error.config !== undefined ? error.config : '*** no config available!';
          var requestMethod = error.config !== undefined ? error.config.method : '*** unknown method';
          var errorInfo = JSON.stringify({
            type: 'FrontendFailedRequestError',
            url: failedRequestUrl,
            statusCode: statusCode,
            statusText: statusText,
            errorData: errorData,
            requestMethod: requestMethod
          });
          var errorId = failedRequestUrl + ' - ' + statusCode;
          if (loggedErrors.indexOf(errorId) !== -1) {
            console.log("Error with id has already been logged, aborting! ", errorId);
            duplicates_skipped += 1;
            return error;
          } else {
            errors += 1;
            loggedErrors.push(errorId)
          }
          $http.post(logEndpoint, errorInfo)
            .then(function (success) {
                console.log("Failed resource call successfully logged to backend");
              },
              function (failure) {
                console.log("Failed resource call detected, logging to backend failed");
              }
            );
        }
      } catch (e) {
        console.log("Something went wrong while trying to log backend rest error: " , e)
      }
      return error;
    }
  }
}]
