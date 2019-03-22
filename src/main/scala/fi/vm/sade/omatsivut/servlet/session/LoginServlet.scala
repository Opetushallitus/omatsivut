package fi.vm.sade.omatsivut.servlet.session

import fi.vm.sade.omatsivut.servlet.{OmatSivutServletBase, ServerContextPath}

class LoginServlet() extends OmatSivutServletBase with ShibbolethPaths {
  get("/*") {
    val noContextPath: Boolean = request.getContextPath.isEmpty
    redirectToShibbolethLogin(ServerContextPath(request).path, noContextPath ,response)
  }
}
