package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.json.JsonFormats
import org.scalatra.NotFound
import org.scalatra.json.JacksonJsonSupport

trait KoulutusServletContainer {

  class KoulutusServlet extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {
    before() {
      contentType = formats("json")
    }

    get("/opetuspisteet/:query") {
      NotFound("error" -> "Not found")
    }

    get("/koulutukset/:asId/:opetuspisteId") {
      NotFound("error" -> "Not found")
    }

    get("/koulutus/:aoId") {
      NotFound("error" -> "Not found")
    }

  }
}

