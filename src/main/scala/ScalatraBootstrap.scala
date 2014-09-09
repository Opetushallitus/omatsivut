import javax.servlet.ServletContext
import fi.vm.sade.omatsivut.config.{OmatSivutSpringContext, AppConfig}
import AppConfig.AppConfig
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.servlet.session.{SecuredSessionServlet, SessionServlet, LogoutServlet, LoginServlet}
import fi.vm.sade.omatsivut.servlet.testing.{FakeShibbolethServlet, TestHelperServlet}
import org.scalatra._
import fi.vm.sade.omatsivut.config.ScalatraPaths

class ScalatraBootstrap extends LifeCycle {
  implicit val swagger = new OmatSivutSwagger
  implicit val config: AppConfig = AppConfig.fromSystemProperty
  OmatSivutSpringContext.check

  override def init(context: ServletContext) {
    config.start

    context.mount(new ApplicationsServlet, ScalatraPaths.applications)
    context.mount(new TranslationServlet, "/translations")
    context.mount(new KoulutusServlet, ScalatraPaths.koulutusinformaatio)
    context.mount(new SwaggerServlet, "/swagger/*")
    context.mount(new SecuredSessionServlet, "/secure")
    context.mount(new SessionServlet, "/session")
    context.mount(new RaamitServlet, "/raamit")
    context.mount(new LoginServlet, "/login")
    context.mount(new LogoutServlet, "/logout")
    context.mount(new TestHelperServlet, "/util")
    context.mount(new FakeShibbolethServlet, "/Shibboleth.sso")
  }

  override def destroy(context: ServletContext) = {
    config.stop
    super.destroy(context)
  }
}
