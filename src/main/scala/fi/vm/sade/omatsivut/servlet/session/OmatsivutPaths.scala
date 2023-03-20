package fi.vm.sade.omatsivut.servlet.session

import java.net.URLEncoder

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.utils.slf4j.Logging

trait OmatsivutPaths extends Logging {
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

  private def urlPrefixFromDomain(domain: String, lang: String): String = {
    val host = OphUrlProperties.url("host.haku")
    val protocol = if (host.startsWith("http")) "" else "https://"
    protocol + domain
  }

  def initsessionPath(contextPath: String)(implicit lang: Language.Language, domain: String): String = {
    val realContextPath = getContextPath(contextPath)
    val urlRoot = urlPrefixFromDomain(domain, lang.toString.toLowerCase())
    val sessionPath = urlRoot + realContextPath + "/initsession"
    logger.info(s"Domain: $domain | Language: $lang | SessionPath: $sessionPath")
    sessionPath
  }

  def logoutServletPath(contextPath: String)(implicit lang: Language.Language): String = {
    val realContextPath = getContextPath(contextPath)
    val urlRoot = urlPrefix(lang.toString.toLowerCase())
    urlRoot + realContextPath + "/logout"
  }

  def loginPath(contextPath: String)(implicit lang: Language.Language, domain: String): String = {
    val realContextPath = getContextPath(contextPath)
    val urlRoot = urlPrefixFromDomain(domain, lang.toString.toLowerCase())
    val urlCas  = urlPrefix("fi")
    val fullUrl = urlRoot + realContextPath + "/initsession"
    urlCas + "/cas-oppija/login?locale=" + lang + "&valtuudet=" + AppConfig.suomifi_valtuudet_enabled.toString + "&service=" + URLEncoder.encode(fullUrl)
  }

  def logoutPath(servicePath: String)(implicit lang: Language.Language): String = {
    val urlRoot = urlPrefix(lang.toString.toLowerCase())
    val urlCas = urlPrefix("fi")
    val fullUrl = urlRoot + servicePath
    urlCas + "/cas-oppija/logout?service=" + URLEncoder.encode(fullUrl)
  }

  def omatsivutPath(contextPath: String)(implicit lang: Language.Language): String = {
    val realContextPath = getContextPath(contextPath)
    urlPrefix(lang.toString.toLowerCase) + realContextPath
  }
}
