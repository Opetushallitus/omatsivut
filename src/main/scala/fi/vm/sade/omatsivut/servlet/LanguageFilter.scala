package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraFilter

class LanguageFilter extends ScalatraFilter with Logging {
  val cookieName = "i18next"
  val cookieMaxAge = 60 * 60 * 24 * 1800

  val domainFi = "opintopolku.fi"
  val domainSv = "studieinfo.fi"
  val domainEn = "studyinfo.fi"

  before() {
    checkLanguage(request, response)
  }

  private def checkLanguage(request: HttpServletRequest, response: HttpServletResponse) {
    val (lang: Language.Language, setCookie: Boolean) = chooseLanguage(Option(request.getParameter("lang")), Option(request.getCookies()), getRealUrl(request))
    if (setCookie) {
      addCookie(response, lang)
    }
    request.setAttribute("lang", lang)
  }

  def chooseLanguage(param: Option[String], cookies: Option[Array[Cookie]], url: String): (Language.Language, Boolean) = {
    val language = chooseLanguageFromParam(param)
      .orElse(chooseLanguageFromCookie(cookies))
      .orElse(chooseLanguageFromHost(url))
      .getOrElse{
        logger.warn("Could not decide language from params, cookies, or URL. Using default language.")
        Language.fi
      }
    (language, true)
  }

  private def chooseLanguageFromCookie(optCookies: Option[Array[Cookie]]): Option[Language] = {
    (for {
      cookies <- optCookies
      cookie <- cookies.find(_.getName.equals(cookieName))
      lang <- Language.parse(cookie.getValue)
    } yield lang)
  }

  private def addCookie(response: HttpServletResponse, lang: Language.Language) {
    val cookie = new Cookie(cookieName, lang.toString())
    cookie.setMaxAge(cookieMaxAge)
    cookie.setPath("/")
    response.addCookie(cookie)
  }

  private def chooseLanguageFromParam(param: Option[String]): Option[Language.Language] = {
    param.flatMap(Language.parse(_))
  }

  private def getRealUrl(request: HttpServletRequest): String = {
    request.header("X-Forwarded-Host")
      .orElse(request.header("Referer"))
      .getOrElse{
      logger.warn(s"Headers X-Forwarded-Host and Referer were not set, reading language from request url.")
      request.getRequestURL.toString
    }
  }

  private def chooseLanguageFromHost(url: String): Option[Language.Language] = {
    (url match {
      case x if x.contains(domainFi) => Some(Language.fi)
      case x if x.contains(domainSv) => Some(Language.sv)
      case x if x.contains(domainEn) => Some(Language.en)
      case default => None
    })
  }
}
