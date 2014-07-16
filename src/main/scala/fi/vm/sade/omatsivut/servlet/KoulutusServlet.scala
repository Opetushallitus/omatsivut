package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class KoulutusServlet(implicit val swagger: Swagger, val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään opetuspisteitä ja koulutuksia"

  before() {
    contentType = formats("json")
  }

  get("/opetuspisteet/:query") {
    KoulutusInformaatioService.opetuspisteet(params("asId"), params("query"))
  }

  get("/koulutukset/:asId/:opetuspisteId") {
    KoulutusInformaatioService.koulutukset(params("asId"), params("opetuspisteId"), params("baseEducation"), params("vocational"), params("uiLang"))
  }
}
