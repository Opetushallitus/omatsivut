package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.servlet.{OmatSivutServletBase}

class LoginServlet() extends OmatSivutServletBase with OmatsivutPaths {
  get("/*") {
    response.redirect(shibbolethPath(request.getContextPath))
  }
}
