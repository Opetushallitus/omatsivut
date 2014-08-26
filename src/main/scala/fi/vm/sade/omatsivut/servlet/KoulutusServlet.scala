package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}
import scala.collection.TraversableOnce

class KoulutusServlet(implicit val swagger: Swagger, val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään opetuspisteitä ja koulutuksia"
  val koulutusInformaatio = KoulutusInformaatioService.apply

  before() {
    contentType = formats("json")
  }

  get("/opetuspisteet/:query") {
    checkNotFound(koulutusInformaatio.opetuspisteet(params("asId"), params("query")))
  }

  get("/koulutukset/:asId/:opetuspisteId") {
    checkNotFound(koulutusInformaatio.koulutukset(params("asId"), params("opetuspisteId"), paramOption("baseEducation"), params("vocational"), params("uiLang")))
  }

  get("/koulutus/:aoId") {
    checkNotFound(koulutusInformaatio.koulutus(params("aoId")))
  }

  private def checkNotFound(result: TraversableOnce[Any]) = {
    if(result.isEmpty) {
      response.setStatus(404)
    }
    result
  }
}
