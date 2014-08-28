package fi.vm.sade.omatsivut.servlet

import javax.servlet._
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Language

class LanguageFilter extends Filter with Logging{

  val cookieName = "i18next"
  val cookieMaxAge = 60 * 60 * 24 * 1800

  import collection.JavaConversions._

  def doFilter(req: ServletRequest, res: ServletResponse, filterChain: FilterChain) {
    checkLanguage(req.asInstanceOf[HttpServletRequest], res.asInstanceOf[HttpServletResponse])
    filterChain.doFilter(req, res)
  }

  def checkLanguage(request: HttpServletRequest, response: HttpServletResponse) {
    val (lang: Language.Language, setCookie: Boolean) = chooseLanguage(Option(request.getParameter("lang")), Option(request.getCookies()))
    if(setCookie) {
      addCookie(response, lang)
    }
    request.setAttribute("lang", lang)
  }

  def chooseLanguage(paramVal: Option[String], cookies: Option[Array[Cookie]]): (Language.Language, Boolean) = {
    paramVal match {
      case Some(langStr) => {
        Language.parse(langStr) match {
          case Some(lang) => (lang, true)
          case None => {
            logger.warn("Unsupported language '" + langStr + "' using 'fi' instead")
            (Language.fi, true)
          }
        }
      }
      case None => (langCookie(cookies).getOrElse(Language.fi), true)
    }
  }

  private def langCookie(cookies: Option[Array[Cookie]]) = {
    reqCookie(cookies, {_.getName.equals(cookieName)})
  }

  private def reqCookie(optCookies: Option[Array[Cookie]], matcher: (Cookie) => Boolean) = {
    for {
      cookies <- optCookies
      cookie <- cookies.find(matcher)
      lang <- Language.parse(cookie.getValue())
    } yield lang
  }

  def addCookie(response: HttpServletResponse, lang: Language.Language) {
    val cookie = new Cookie(cookieName, lang.toString())
    cookie.setMaxAge(cookieMaxAge)
    cookie.setPath("/")
    response.addCookie(cookie)
  }

  def init(filterConfig: FilterConfig) {
  }

  def destroy {
  }
}