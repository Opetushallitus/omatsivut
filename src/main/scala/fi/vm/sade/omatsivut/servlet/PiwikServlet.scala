package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig

/**
 *
 */
class PiwikServlet(val appConfig: AppConfig) extends OmatSivutServletBase {

  before() {
    contentType = "application/javascript"
  }

  get("/load") {
"""
var siteDomain = document.domain;
var matomoSiteId;
switch (siteDomain) {
    case "opintopolku.fi":
        matomoSiteId = 4;
        break;
    case "studieinfo.fi":
        matomoSiteId = 13;
        break;
    case "studyinfo.fi":
        matomoSiteId = 14;
        break;
    case "virkailija.opintopolku.fi":
        matomoSiteId = 3;
        break;
    case "testiopintopolku.fi":
    case "testistudieinfo.fi":
    case "testistudyinfo.fi":
        matomoSiteId = 1;
        break;
    case "virkailija.testiopintopolku.fi":
        matomoSiteId = 5;
        break;
    case "demo-opintopolku.fi":
    case "demo-studieinfo.fi":
    case "demo-studyinfo.fi":
        matomoSiteId = 15;
        break;
    default:
        matomoSiteId = 2; // Kehitys
}

var _paq = window._paq || [];
/* tracker methods like "setCustomDimension" should be called before "trackPageView" */
_paq.push(['trackPageView']);
_paq.push(["setDocumentTitle", document.domain + "/" + document.title]);
_paq.push(['enableLinkTracking']);
(function() {
  var u="//analytiikka.opintopolku.fi/matomo/";
  _paq.push(['setTrackerUrl', u+'matomo.php']);
  _paq.push(['setSiteId', matomoSiteId]);
  var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
  g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'matomo.js'; s.parentNode.insertBefore(g,s);
})();
"""
  }

  get("/loadKeha") {
"""
var siteDomain = document.domain;
var matomoSiteUrl;
switch (siteDomain) {
    case "opintopolku.fi":
    case "studieinfo.fi":
    case "studyinfo.fi":
        matomoSiteUrl = "https://analytiikka.ahtp.fi/";
        break;
    default:
        matomoSiteUrl = "https://keha-matomo-sdg-qa-qa.azurewebsites.net/"; // Testi
}
var cookieconsentSettings = {
    // Urls where matomo files can be found on the (matomo) server.
    matomoSiteUrl: matomoSiteUrl,
    matomoSiteId: "8",
    // Params that are included in the tracking requests. See https://developer.matomo.org/api-reference/tracking-api
    includedParams: ["idsite", "rec", "action_name", "url", "_id", "rand", "apiv"],
};
var hasInit = false;
var initMatomoTracker = function () {
    try {
        if (hasInit) return;
        hasInit = true;
        var tracker;
        if (typeof Matomo !== 'undefined') {
            tracker = Matomo;
        } else {
            tracker = Piwik;
        }
        var url = cookieconsentSettings.matomoSiteUrl;
        var fixedUrl = url.charAt(url.length - 1) === '/' ? url : url + '/';
        matomoTracker = tracker.getTracker(fixedUrl + "matomo.php", cookieconsentSettings.matomoSiteId);
        var customRequestProcess = function (request) {
            try {
                var pairs = request.split("&");
                var requestParametersArray = [];
                for (var index = 0; index < pairs.length; ++index) {
                    var pair = pairs[index].split("=");
                    if (cookieconsentSettings.includedParams.indexOf(pair[0]) === -1) {
                        continue;
                    }
                    requestParametersArray.push(pair[0] + "=" + pair[1]);
                }
                var osIndex = navigator.userAgent.indexOf(")");
                var ua =
                    osIndex !== -1
                        ? navigator.userAgent.substring(0, osIndex + 1)
                        : "Mozilla/5.0";
                requestParametersArray.push("ua=" + ua);
                return requestParametersArray.join("&");
            } catch (err) {
                return request;
            }
        };
        matomoTracker.setCustomRequestProcessing(customRequestProcess);
        matomoTracker.trackPageView();
        matomoTracker.enableLinkTracking();
    } catch (err) { }
};
if (typeof Matomo === 'undefined') {
    window.matomoAsyncInit = initMatomoTracker;
    window.piwikAsyncInit = initMatomoTracker;
}
else {
    initMatomoTracker();
}
"""
  }
}
