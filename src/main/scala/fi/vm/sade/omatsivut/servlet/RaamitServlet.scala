package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class RaamitServlet(val appConfig: AppConfig) extends OmatSivutServletBase {

  before() {
    contentType ="application/javascript"
  }

  get("/*") {
"""if (document.location.hash.indexOf("skipRaamit") < 0) {
  var raamit = document.getElementById("apply-raamit");
  if(!raamit) {
      raamit = document.createElement("script");
      raamit.id = "apply-raamit";
      raamit.src = """" + appConfig.settings.raamitUrl  + """/oppija-raamit/apply-raamit.js";
      document.getElementsByTagName("head")[0].appendChild(raamit);
  }
}"""
  }
}
