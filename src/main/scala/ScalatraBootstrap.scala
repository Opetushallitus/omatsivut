import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.omatsivut.servlet._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap extends LifeCycle {
  implicit val swagger = new OmatSivutSwagger
  var mongo: Option[MongoServer] = None
  OmatSivutSpringContext.check

  override def init(context: ServletContext) {
    val config: AppConfig = AppConfig.config
    if (config == AppConfig.IT) {
      mongo = EmbeddedMongo.start
    }

    implicit val authService = config.authenticationInfoService

    context.mount(new ApplicationsServlet, "/api")
    context.mount(new SwaggerServlet, "/swagger/*")
    context.mount(new SessionServlet, "/secure")
    context.mount(new TestHelperServlet(config), "/util")
  }

  override def destroy(context: ServletContext) = {
    mongo.foreach(_.destroy)
    super.destroy(context)
  }
}