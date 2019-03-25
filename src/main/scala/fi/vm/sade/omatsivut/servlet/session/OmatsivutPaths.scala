package fi.vm.sade.omatsivut.servlet.session

import java.net.URLEncoder

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.OphUrlProperties

trait OmatsivutPaths {
  private def urlEncode(str: String): String = URLEncoder.encode(str, "UTF-8")

  private def hostHakuParameterName(lang: String): String = {
    val hostHakuBase = "host.haku"
    val hostHakuSuffix = lang match {
      case "en" | "sv" => lang
      case _ => ""
    }
    hostHakuBase + hostHakuSuffix
  }

  private def urlPrefix(lang: String): String = {
    val host = OphUrlProperties.url(hostHakuParameterName(lang))
    "https://" + host
  }

  def shibbolethPath(contextPath: String)(implicit lang: Language.Language): String = {
    val realContextPath = if (contextPath.isEmpty) "/omatsivut" else contextPath
    OphUrlProperties.url("shibboleth.login", lang.toString().toUpperCase()) +
      "?target=" + urlEncode(urlPrefix(lang.toString.toLowerCase) + realContextPath + "/initsession/")
  }
}
