package fi.vm.sade.omatsivut.servlet.session

import java.net.URLEncoder

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.OphUrlProperties
import org.scalatra.servlet.RichResponse

trait ShibbolethPaths {
  def urlEncode(str: String): String = URLEncoder.encode(str, "UTF-8")

  def shibbolethPath(targetUrlPrefix: String)(implicit lang: Language.Language): String = {
    OphUrlProperties.url("shibboleth.login", lang.toString().toUpperCase()) +
      "?target=" + urlEncode(targetUrlPrefix + "/omatsivut/initsession")
  }

  def redirectToShibbolethLogin(targetUrlPrefix: String, response: RichResponse)(implicit lang: Language.Language) {
    response.redirect(shibbolethPath(targetUrlPrefix))
  }
}
