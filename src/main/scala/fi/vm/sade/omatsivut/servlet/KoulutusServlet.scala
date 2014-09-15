package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioService, KoulutusInformaatioComponent}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}
import fi.vm.sade.omatsivut.domain.Language

trait KoulutusServletComponent {
  this: KoulutusInformaatioComponent =>

  val koulutusInformaatioService: KoulutusInformaatioService

  class KoulutusServlet(implicit val swagger: Swagger, val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään opetuspisteitä ja koulutuksia"

    before() {
      contentType = formats("json")
    }

    get("/opetuspisteet/:query") {
      checkNotFound(koulutusInformaatioService.opetuspisteet(params("asId"), params("query"), paramOption("lang").getOrElse(Language.fi.toString())))
    }

    get("/koulutukset/:asId/:opetuspisteId") {
      checkNotFound(koulutusInformaatioService.koulutukset(params("asId"), params("opetuspisteId"), paramOption("baseEducation"), params("vocational"), paramOption("uiLang").getOrElse(Language.fi.toString())))
    }

    get("/koulutus/:aoId") {
      checkNotFound(koulutusInformaatioService.koulutus(params("aoId"), paramOption("lang").getOrElse(Language.fi.toString())))
    }

    private def checkNotFound[A](result: Option[A]) = {
      result match {
        case Some(x) => x
        case _ => response.setStatus(404)
      }
    }
  }
}

