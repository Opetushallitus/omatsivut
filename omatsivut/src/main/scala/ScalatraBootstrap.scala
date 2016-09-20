import java.util
import javax.servlet.{DispatcherType, ServletContext}

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.{OmatSivutSpringContext, AppConfig, ComponentRegistry}
import fi.vm.sade.omatsivut.security.fake.{FakeShibbolethFilter, FakeShibbolethServlet}
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.servlet.session.{LoginServlet, SessionServlet}
import fi.vm.sade.utils.slf4j.Logging
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
    context.addFilter("AuditLoginFilter", componentRegistry.newAuditLoginFilter)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")
    context.addFilter("CacheControl", new CacheControlFilter)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")
    context.addFilter("Language", new LanguageFilter)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")

    context.mount(componentRegistry.newApplicationsServlet, "/secure/applications")
    context.mount(componentRegistry.newTuloskirjeetServlet, "/tuloskirjeet")
    context.mount(componentRegistry.newNonSensitiveApplicationServlet, "/insecure/applications")
    context.mount(new TranslationServlet, "/translations")
    context.mount(componentRegistry.newMuistilistaServlet, "/muistilista")
    context.mount(componentRegistry.newKoodistoServlet, "/koodisto")
    context.mount(componentRegistry.newKoulutusServlet, "/koulutusinformaatio")
    context.mount(componentRegistry.newSecuredSessionServlet, "/secure")
    context.mount(new SessionServlet(config), "/session")
    context.mount(new RaamitServlet(config), "/raamit")
    context.mount(new PiwikServlet(config), "/piwik")
    context.mount(new LoginServlet(config.authContext), "/login")
    context.mount(componentRegistry.newLogoutServlet, "/logout")
    context.mount(componentRegistry.newFixtureServlet, "/util")
    context.mount(new HealthServlet, "/health")
  }

  override def destroy(context: ServletContext) = {
    globalRegistry.foreach(_.stop)
    super.destroy(context)
  }
}
