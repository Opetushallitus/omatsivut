package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.localization.OmatSivutTranslations
import org.scalatra.json.JacksonJsonSupport

class TranslationServlet extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {

  before() {
    contentType = formats("json")
  }

  get() {
    OmatSivutTranslations.getTranslations
  }
}
