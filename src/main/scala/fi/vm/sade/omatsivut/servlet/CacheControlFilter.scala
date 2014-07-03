package fi.vm.sade.omatsivut.servlet

import javax.servlet._
import javax.servlet.http.HttpServletResponse

class CacheControlFilter extends Filter {
  def doFilter(req: ServletRequest, res: ServletResponse, filterChain: FilterChain) {
    doNotCache(res.asInstanceOf[HttpServletResponse])
    filterChain.doFilter(req, res)
  }

  def doNotCache(response: HttpServletResponse) {
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, proxy-revalidate, no-transform")
    response.setHeader("Pragma", "no-cache")
    response.setHeader("Expires", "0")
  }

  def init(filterConfig: FilterConfig) {
  }

  def destroy {
  }
}