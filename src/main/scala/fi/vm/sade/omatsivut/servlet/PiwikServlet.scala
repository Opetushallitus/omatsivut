package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig

/**
 *
 */
class PiwikServlet(val appConfig: AppConfig) extends OmatSivutServletBase {

  before() {
    contentType = "application/javascript"
  }

  private def onlyOnProductionAndQA(f: => String) = {
    if (appConfig.settings.environment.isQA || appConfig.settings.environment.isProduction) {
      f
    } else {
      "/*" + f + "*/"
    }
  }

  get("/*") {
    onlyOnProductionAndQA {
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
    case "testi.opintopolku.fi":
    case "testi.studieinfo.fi":
    case "testi.studyinfo.fi":
        matomoSiteId = 1;
        break;
    case "testi.virkailija.opintopolku.fi":
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
  }
}
