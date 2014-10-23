import java.util
import javax.servlet.{DispatcherType, ServletContext}

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.{ComponentRegistry, AppConfig, OmatSivutSpringContext}
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.servlet.session.{LoginServlet, SessionServlet}
import fi.vm.sade.omatsivut.servlet.testing.{FakeShibbolethFilter, FakeShibbolethServlet}
import fi.vm.sade.omatsivut.util.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging {

  var globalRegistry: Option[ComponentRegistry] = None

  OmatSivutSpringContext.check

  override def init(context: ServletContext) {
    val config: AppConfig = AppConfig.fromOptionalString(Option(context.getAttribute("omatsivut.profile").asInstanceOf[String]))
    val componentRegistry = new ComponentRegistry(config)

    componentRegistry.start
    globalRegistry = Some(componentRegistry)

    if(config.usesFakeAuthentication) {
      logger.info("Using fake authentication")
      context.addFilter("FakeShibboleth", new FakeShibbolethFilter)
        .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true,  "/", "/index.html", "/secure/*")
      context.mount(new FakeShibbolethServlet(config), "/Shibboleth.sso")
    }

    context.addFilter("UserFilter", new UserFilter(config))
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/index.html", "/")

    context.addFilter("CacheControl", new CacheControlFilter)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")
    context.addFilter("Language", new LanguageFilter)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    context.mount(componentRegistry.newApplicationsServlet, "/secure/applications")
    context.mount(new TranslationServlet, "/translations")
    context.mount(componentRegistry.newKoodistoServlet, "/koodisto")
    context.mount(componentRegistry.newKoulutusServlet, "/koulutusinformaatio")
    context.mount(componentRegistry.newSwaggerServlet, "/swagger/*")
    context.mount(componentRegistry.newSecuredSessionServlet, "/secure")
    context.mount(new SessionServlet(config), "/session")
    context.mount(new RaamitServlet(config), "/raamit")
    context.mount(new LoginServlet(config), "/login")
    context.mount(componentRegistry.newLogoutServlet, "/logout")
    context.mount(componentRegistry.newTestHelperServlet, "/util")
  }

  override def destroy(context: ServletContext) = {
    globalRegistry.foreach(_.stop)
    super.destroy(context)
  }
}
