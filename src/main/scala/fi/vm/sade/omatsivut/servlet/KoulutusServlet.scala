package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import org.scalatra.NotFound
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}

trait KoulutusServletContainer {
  this: KoulutusInformaatioComponent =>

  val koulutusInformaatioService: KoulutusInformaatioService

  class KoulutusServlet(implicit val swagger: Swagger) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään opetuspisteitä ja koulutuksia"

    before() {
      contentType = formats("json")
    }

    get("/opetuspisteet/:query") {
      checkNotFound(koulutusInformaatioService.opetuspisteet(params("asId"), params("query"), getLangParam("lang")))
    }

    get("/koulutukset/:asId/:opetuspisteId") {
      checkNotFound(koulutusInformaatioService.koulutukset(params("asId"), params("opetuspisteId"), paramOption("baseEducation"), params("vocational"), getLangParam("uiLang")))
    }

    get("/koulutus/:aoId") {
      checkNotFound(koulutusInformaatioService.koulutus(params("aoId"), getLangParam("lang")))
    }

    def getLangParam(param: String): Language.Value = {
      paramOption(param).flatMap(Language.parse(_)).getOrElse(Language.fi)
    }

    private def checkNotFound[A](result: Option[A]) = {
      result match {
        case Some(x) => x
        case _ => NotFound("error" -> "Not found")
      }
    }
  }
}

