package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.HakemusEditoriComponent
import fi.vm.sade.hakemuseditori.json.JsonFormats
import org.scalatra.NotFound
import org.scalatra.json.JacksonJsonSupport

trait KoodistoServletContainer {
  this: HakemusEditoriComponent =>

  class KoodistoServlet extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {
    before() {
      contentType = formats("json")
    }

    get("/postitoimipaikka/:postalCode") {
      NotFound("error" -> "Not found") // ei pitäisi enää tarvita tätä
    }
  }
}

