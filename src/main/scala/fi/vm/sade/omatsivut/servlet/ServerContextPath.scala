package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.HttpServletRequest

case class ServerContextPath(request: HttpServletRequest) {

  val path = "https://" + request.getServerName() + request.getContextPath()

}
