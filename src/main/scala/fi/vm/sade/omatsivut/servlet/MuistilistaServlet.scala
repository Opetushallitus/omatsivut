package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.util.Logging
import org.scalatra.json.JacksonJsonSupport

trait MuistilistaServletContainer {

  class MuistilistaServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with Logging {

    get("/:compressedMuistilista") {
      // vanhan koulutusinformaation toiminnallisuus ei ole enää käytössä
      response.setStatus(403)
      "403 Forbidden"
    }

    post() {
      // vanhan koulutusinformaation toiminnallisuus ei ole enää käytössä
      response.setStatus(403)
      "403 Forbidden"
    }

  }

}
