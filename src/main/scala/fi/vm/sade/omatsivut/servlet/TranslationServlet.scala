package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.localization.Translations
import org.scalatra.json.JacksonJsonSupport

class TranslationServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {

  before() {
    contentType = formats("json")
  }

  get() {
    Translations.getTranslations
  }
}
