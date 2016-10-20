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
"""var piwik = document.getElementById('apply-piwik');
if (!piwik) {
  piwik = document.createElement('script');
  piwik.id = 'apply-piwik';
  piwik.src = '""" + OphUrlProperties.url("piwik.url") +  """';
  document.getElementsByTagName("head")[0].appendChild(piwik);
}"""
    }
  }
}
