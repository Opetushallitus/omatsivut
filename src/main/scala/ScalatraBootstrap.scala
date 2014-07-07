import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.servlet._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  implicit val swagger = new OmatSivutSwagger
  OmatSivutSpringContext.check

  override def init(context: ServletContext) {
    implicit val authService = AppConfig.config.authenticationInfoService

    context.mount(new ApplicationsServlet, "/api")
    context.mount(new SwaggerServlet, "/swagger/*")
    context.mount(new SessionServlet, "/secure")
    context.mount(new TestHelperServlet(AppConfig.config), "/util")
  }
}