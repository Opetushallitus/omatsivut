package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.HttpServletResponse

import org.scalatra.ScalatraFilter

class CacheControlFilter extends OmatSivutFilterBase {
  before() {
    doNotCache(response)
  }

  def doNotCache(response: HttpServletResponse) {
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, proxy-revalidate, no-transform")
    response.setHeader("Pragma", "no-cache")
    response.setHeader("Expires", "0")
  }
}
