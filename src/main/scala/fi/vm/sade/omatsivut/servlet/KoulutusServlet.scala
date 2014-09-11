package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class KoulutusServlet(implicit val swagger: Swagger, val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään opetuspisteitä ja koulutuksia"
  val koulutusInformaatio = appConfig.componentRegistry.koulutusInformaatioService

  before() {
    contentType = formats("json")
  }

  get("/opetuspisteet/:query") {
    checkNotFound(koulutusInformaatio.opetuspisteet(params("asId"), params("query"), params("lang")))
  }

  get("/koulutukset/:asId/:opetuspisteId") {
    checkNotFound(koulutusInformaatio.koulutukset(params("asId"), params("opetuspisteId"), paramOption("baseEducation"), params("vocational"), params("uiLang")))
  }

  get("/koulutus/:aoId") {
    checkNotFound(koulutusInformaatio.koulutus(params("aoId"), params("lang")))
  }

  private def checkNotFound[A](result: Option[A]) = {
    result match {
      case Some(x) => x
      case _ => response.setStatus(404)
    }
  }
}
