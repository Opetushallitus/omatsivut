package fi.vm.sade.omatsivut.servlet.session

import java.net.URLEncoder

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.OphUrlProperties
import org.scalatra.servlet.RichResponse

trait ShibbolethPaths {
  def urlEncode(str: String): String = URLEncoder.encode(str, "UTF-8")

  def shibbolethPath(targetUrlPrefix: String, noContextPath: Boolean)(implicit lang: Language.Language): String = {
    val contextPath = if (noContextPath) "/omatsivut" else ""
    OphUrlProperties.url("shibboleth.login", lang.toString().toUpperCase()) +
      "?target=" + urlEncode(targetUrlPrefix + contextPath + "/initsession")
  }

  def redirectToShibbolethLogin(targetUrlPrefix: String, noContextPath: Boolean, response: RichResponse)(implicit lang: Language.Language) {
    response.redirect(shibbolethPath(targetUrlPrefix, noContextPath))
  }
}
