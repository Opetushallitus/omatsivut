import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.omatsivut.servlet._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  implicit val swagger = new OmatSivutSwagger
  implicit val config: AppConfig = AppConfig.fromSystemProperty
  OmatSivutSpringContext.check

  override def init(context: ServletContext) {
    config.start

    context.mount(new ApplicationsServlet, "/api")
    context.mount(new KoulutusServlet, "/koulutusinformaatio")
    context.mount(new SwaggerServlet, "/swagger/*")
    context.mount(new SessionServlet, "/secure")
    context.mount(new TestHelperServlet, "/util")
  }

  override def destroy(context: ServletContext) = {
    config.stop
    super.destroy(context)
  }
}