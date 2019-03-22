package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.servlet.{OmatSivutServletBase, ServerContextPath}

class LoginServlet() extends OmatSivutServletBase with ShibbolethPaths {
  get("/*") {
    redirectToShibbolethLogin(ServerContextPath(request).path, response)
  }
}
