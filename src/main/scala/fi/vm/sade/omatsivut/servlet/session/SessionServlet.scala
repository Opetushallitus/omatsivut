package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

class SessionServlet(implicit val appConfig: AppConfig) extends OmatSivutServletBase {
  get("/reset") {
    redirectToIndex
  }

  def redirectToIndex {
    val redirectUrl = if (appConfig.usesFakeAuthentication) request.getContextPath + "/index.html" else "/"
    response.redirect(redirectUrl)
  }
}
