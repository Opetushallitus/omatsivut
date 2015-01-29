package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koodisto.{KoodistoService, KoodistoComponent}
import org.scalatra.NotFound
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{SwaggerSupport, Swagger}

trait KoodistoServletContainer {
  this: KoodistoComponent =>

  val koodistoService: KoodistoService

  class KoodistoServlet(implicit val swagger: Swagger) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään koodiston arvoja"

    before() {
      contentType = formats("json")
    }

    get("/postitoimipaikka/:postalCode") {
      val office = koodistoService.postOffice(params("postalCode"))
      checkNotFound(office.map((translations: Map[String, String]) => PostOffice(params("postalCode"), translations.getOrElse(language.toString, ""))))
    }

    case class PostOffice(postalCode: String, postOffice: String)

    private def checkNotFound[A](result: Option[A]) = {
      result match {
        case Some(x) => x
        case _ => NotFound("error" -> "Not found")
      }
    }
  }
}

