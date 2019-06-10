package fi.vm.sade.omatsivut.servlet

import javax.servlet.http.HttpServletRequest
import org.scalatra.{RouteTransformer, ScalatraFilter}

trait ScalatraFilterWithWhitelist extends ScalatraFilter {

  val whitelistedServletPaths: Seq[String]

  override def before(transformers: RouteTransformer*)(fun: => Any): Unit = {
    def isWhitelisted(request: HttpServletRequest): Boolean = {
      whitelistedServletPaths.contains(request.getServletPath)
    }

    def wrappedFun = if (!isWhitelisted(request)) { fun }
    super.before(transformers: _*)(wrappedFun)
  }
}
