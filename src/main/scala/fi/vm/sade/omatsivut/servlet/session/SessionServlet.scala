package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.servlet.OmatSivutServletBase

class SessionServlet extends OmatSivutServletBase {
  get("/reset") {
    redirectToIndex
  }

  def redirectToIndex {
    // TODO: Redirect to domain root when login links in place, e.g. :
    // val redirectUrl = if (appConfig.usesFakeAuthentication) request.getContextPath + "/index.html" else "/"
    // response.redirect(redirectUrl)
    response.redirect(request.getContextPath + "/index.html")
  }
}
