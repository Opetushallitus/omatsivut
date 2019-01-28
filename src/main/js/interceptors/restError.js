//import { getBearerToken, setBearerToken } from '../util';

function shouldRerouteRequest(config) {
  var decodedUrl = decodeURIComponent(window.location.href);
  var pageReachedBySecureLink = (decodedUrl.includes('hakutoiveidenMuokkaus.html') || decodedUrl.includes('token'));
  return pageReachedBySecureLink && config.url.includes('/secure/')
}

function shouldAuthenticate(config) {
  return config.url.includes('insecure/')
}

export default ["$injector", function RestErrorInterceptor($injector) {
  return {
    responseError: function (error) {
      var $http = $injector.get("$http");
      var logEndpoint = window.url("omatsivut.errorlogtobackend");
      if (error === undefined) {
        return error;
      }
      try {
        var failedRequestUrl = (error.config !== undefined && error.config.url !== undefined) ? error.config.url : 'unknown url';
        if (failedRequestUrl.indexOf(logEndpoint) !== -1) {
          console.log("Error came from logging endpoint, won't try to log it to avoid a loop")
        } else {
          var errorData = error.data !== undefined ? JSON.stringify(error.data) : '';
          var statusCode = error.status !== undefined ? error.status : '-1';
          var statusText = error.statusText !== undefined ? error.statusText : '';
          var errorInfo = JSON.stringify({
            type: 'FrontendFailedRequestError',
            url: failedRequestUrl,
            statusCode: statusCode,
            statusText: statusText,
            errorData: errorData
          });
          //console.log("kissaD (debug) logging to backend: ", errorInfo);
          $http.post(logEndpoint, errorInfo)
            .then(function (success) {
                console.log("Failed resource call successfully logged to backend");
              },
              function (failure) {
                console.log("Failed resource call detected, logging to backend failed, ", failure);
              }
            );
        }
      } catch (e) {
        console.log("Something went wrong while trying to log backend rest error: " , e)
      }
      //console.log("returning error...");
      return error;
    }
  }
}]
