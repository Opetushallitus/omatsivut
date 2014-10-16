import javax.servlet.ServletContext

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.{ComponentRegistry, AppConfig, OmatSivutSpringContext}
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.servlet.session.{LoginServlet, SessionServlet}
import fi.vm.sade.omatsivut.servlet.testing.FakeShibbolethServlet
import org.scalatra._

class ScalatraBootstrap extends LifeCycle {
  val config: AppConfig = AppConfig.fromSystemProperty
  val componentRegistry: ComponentRegistry = new ComponentRegistry(config)

  OmatSivutSpringContext.check

  override def init(context: ServletContext) {
    componentRegistry.start

    context.mount(componentRegistry.newApplicationsServlet, "/secure/applications")
    context.mount(new TranslationServlet, "/translations")
    context.mount(componentRegistry.newKoulutusServlet, "/koulutusinformaatio")
    context.mount(componentRegistry.newSwaggerServlet, "/swagger/*")
    context.mount(componentRegistry.newSecuredSessionServlet, "/secure")
    context.mount(new SessionServlet(config), "/session")
    context.mount(new RaamitServlet(config), "/raamit")
    context.mount(new LoginServlet(config), "/login")
    context.mount(componentRegistry.newLogoutServlet, "/logout")
    context.mount(componentRegistry.newTestHelperServlet, "/util")
    context.mount(new FakeShibbolethServlet(config), "/Shibboleth.sso")
  }

  override def destroy(context: ServletContext) = {
    componentRegistry.stop
    super.destroy(context)
  }
}
