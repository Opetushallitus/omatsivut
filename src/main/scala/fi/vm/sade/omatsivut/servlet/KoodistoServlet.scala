package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.HakemusEditoriComponent
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koodisto.{PostOffice, KoodistoService, KoodistoComponent}
import org.scalatra.NotFound
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{SwaggerSupport, Swagger}

trait KoodistoServletContainer {
  this: HakemusEditoriComponent =>

  val koodistoService: KoodistoService

  class KoodistoServlet(implicit val swagger: Swagger) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään koodiston arvoja"

    before() {
      contentType = formats("json")
    }

    get("/postitoimipaikka/:postalCode") {
      checkNotFound(koodistoService.postOffice(params("postalCode"), language))
    }
  }
}

