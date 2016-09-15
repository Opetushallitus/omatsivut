package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.HakemusEditoriComponent
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koodisto.{PostOffice, KoodistoService, KoodistoComponent}
import org.scalatra.NotFound
import org.scalatra.json.JacksonJsonSupport

trait KoodistoServletContainer {
  this: HakemusEditoriComponent =>

  val koodistoService: KoodistoService

  class KoodistoServlet extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {
    before() {
      contentType = formats("json")
    }

    get("/postitoimipaikka/:postalCode") {
      checkNotFound(koodistoService.postOffice(params("postalCode"), language))
    }
  }
}

