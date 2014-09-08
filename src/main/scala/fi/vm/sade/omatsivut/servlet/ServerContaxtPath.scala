package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.HttpServletRequest

case class ServerContaxtPath(request: HttpServletRequest) {

  val path = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath()

}