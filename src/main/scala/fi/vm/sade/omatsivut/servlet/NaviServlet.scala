package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class NaviServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase {

  before() {
    contentType ="application/javascript"
  }

  get("/*") {
"""
var raamit = document.getElementById("apply-raamit");
if(!raamit) {
    raamit = document.createElement("script");
    raamit.id = "apply-raamit";
    raamit.src = """" + appConfig.settings.raamitUrl  + """/apply-raamit.js";
    document.getElementsByTagName("head")[0].appendChild(raamit);
}"""
  }
}
