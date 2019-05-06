package fi.vm.sade.omatsivut.servlet.session

import java.net.URLEncoder

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.OphUrlProperties

trait OmatsivutPaths {
  private def urlEncode(str: String): String = URLEncoder.encode(str, "UTF-8")

  private def getContextPath(contextPathFromRequest: String): String = {
    if (contextPathFromRequest.isEmpty) "/omatsivut" else contextPathFromRequest
  }

  private def hostHakuParameterName(lang: String): String = {
    val hostHakuBase = "host.haku"
    val hostHakuSuffix = lang match {
      case "en" | "sv" => "." + lang
      case _ => ""
    }
    hostHakuBase + hostHakuSuffix
  }

  private def urlPrefix(lang: String): String = {
    val host = OphUrlProperties.url(hostHakuParameterName(lang))
    val protocol = if (host.startsWith("http")) "" else "https://"
    protocol + host
  }

  // magically it will go through shibboleth
  def loginPath(contextPath: String)(implicit lang: Language.Language): String = {
    val realContextPath = getContextPath(contextPath)
    urlPrefix(lang.toString.toLowerCase) + realContextPath + "/initsession/"
  }

  def omatsivutPath(contextPath: String)(implicit lang: Language.Language): String = {
    val realContextPath = getContextPath(contextPath)
    urlPrefix(lang.toString.toLowerCase) + realContextPath
  }
}
