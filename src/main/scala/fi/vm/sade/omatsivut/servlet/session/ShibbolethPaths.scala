package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.domain.Language
import org.scalatra.servlet.RichResponse

trait ShibbolethPaths {
  def redirectToShibbolethLogin(response: RichResponse, ssoContextPath: String)(implicit lang: Language.Language) {
    response.redirect(ssoContextPath + "/Shibboleth.sso/Login" + lang.toString().toUpperCase())
  }
}
