import fi.vm.sade.omatsivut._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {

  implicit val swagger = new OHPSwagger

  override def init(context: ServletContext) {
    AppConfig.loadSettings // <- to fail-fast
    context.mount(new OHPServlet, "/api")
    context.mount(new ResourcesApp, "/swagger/*")
    context.mount(new SessionServlet, "/secure")

    if("true" == System.getProperty("omatsivut.testMode")){
      context.mount(new UtilityServlet, "/util")
    }
  }
}