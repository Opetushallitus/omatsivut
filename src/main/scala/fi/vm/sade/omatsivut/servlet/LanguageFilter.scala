package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.{Cookie, HttpServletRequest, HttpServletResponse}

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra.ScalatraFilter

class LanguageFilter extends ScalatraFilter with Logging{
  val cookieName = "i18next"
  val cookieMaxAge = 60 * 60 * 24 * 1800

  val domainFi = "opintopolku"
  val domainSv = "studieinfo"
  val domainEn = "studyinfo"

  before() {
    checkLanguage(request, response)
  }

  private def checkLanguage(request: HttpServletRequest, response: HttpServletResponse) {
    val (lang: Language.Language, setCookie: Boolean) = chooseLanguage(Option(request.getParameter("lang")), Option(request.getCookies()), request.getRequestURL.toString)
    if(setCookie) {
      addCookie(response, lang)
    }
    request.setAttribute("lang", lang)
  }

  def chooseLanguage(param: Option[String], cookies: Option[Array[Cookie]], url: String): (Language.Language, Boolean) = {
    (chooseLanguageFromParam(param).orElse(chooseLanguageFromCookie(cookies)).orElse(chooseLanguageFromHost(url)).getOrElse(Language.fi), true)
  }

  private def chooseLanguageFromCookie(cookies: Option[Array[Cookie]]) = {
    reqCookie(cookies, {_.getName.equals(cookieName)})
  }

  private def reqCookie(optCookies: Option[Array[Cookie]], matcher: (Cookie) => Boolean) = {
    for {
      cookies <- optCookies
      cookie <- cookies.find(matcher)
      lang <- Language.parse(cookie.getValue())
    } yield lang
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

  private def chooseLanguageFromHost(url: String): Option[Language.Language] = url match {
    case x if x.contains(domainFi) => Some(Language.fi)
    case x if x.contains(domainSv) => Some(Language.sv)
    case x if x.contains(domainEn) => Some(Language.en)
    case default => None
  }
}