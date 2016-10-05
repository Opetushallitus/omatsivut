package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.omatsivut.OphUrlProperties
import org.scalatra.servlet.RichResponse

trait ShibbolethPaths {
  def redirectToShibbolethLogin(response: RichResponse, ssoContextPath: String)(implicit lang: Language.Language) {
    response.redirect(ssoContextPath + OphUrlProperties.url("shibboleth.login", lang.toString().toUpperCase()))
  }
}
