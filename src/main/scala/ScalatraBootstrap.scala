import java.security.Security
import java.util

import javax.servlet.{DispatcherType, ServletContext}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.{AppConfig, ComponentRegistry, OmatSivutSpringContext}
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.servlet.session.{LoginServlet, SessionServlet}
import fi.vm.sade.utils.slf4j.Logging
import org.scalatra._

class ScalatraBootstrap extends LifeCycle with Logging {

  OmatSivutSpringContext.check

  var config: AppConfig = null

  private val healthCheckPath = "/health"
  private val languageFilterWhitelistedServlets: Seq[String] = Seq(healthCheckPath)

  private def assertConfigInitialized(): Unit = {
    if (config == null) throw new RuntimeException(s"config is not initialized")
  }

  override def init(context: ServletContext) {
    config = AppConfig.fromOptionalString(Option(context.getAttribute("omatsivut.profile").asInstanceOf[String]))
    assertConfigInitialized()
    config.onStart()
    val componentRegistry = new ComponentRegistry(config)

    context.addFilter("CacheControl", new CacheControlFilter)
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")
    context.addFilter("Language", new LanguageFilter(languageFilterWhitelistedServlets))
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/*")
    context.addFilter("AuthenticateIfNoSessionFilter", new AuthenticateIfNoSessionFilter(componentRegistry.sessionService))
      .addMappingForUrlPatterns(util.EnumSet.allOf(classOf[DispatcherType]), true, "/", "/index.html")

    context.mount(componentRegistry.newApplicationsServlet, "/secure/applications")
    context.mount(componentRegistry.newValintatulosServlet, "/secure/ilmoittaudu")
    context.mount(componentRegistry.newClientErrorLoggingServlet, "/errorlogtobackend")
    context.mount(componentRegistry.newTuloskirjeetServlet, "/tuloskirjeet")
    context.mount(componentRegistry.newNonSensitiveApplicationServlet, "/insecure")
    context.mount(new TranslationServlet, "/translations")
    context.mount(componentRegistry.newMuistilistaServlet, "/muistilista")
    context.mount(componentRegistry.newKoodistoServlet, "/koodisto")
    context.mount(componentRegistry.newKoulutusServlet, "/koulutusinformaatio")
    context.mount(componentRegistry.newSecuredSessionServlet, "/initsession")
    context.mount(componentRegistry.newSessionServlet, "/session")
    context.mount(new RaamitServlet(config), "/raamit")
    context.mount(new PiwikServlet(config), "/piwik")
    context.mount(new LoginServlet(), "/login")
    context.mount(componentRegistry.newLogoutServlet, "/logout")
    context.mount(componentRegistry.newFixtureServlet, "/util")
    context.mount(new HealthServlet, healthCheckPath)
  }

  override def destroy(context: ServletContext) = {
    assertConfigInitialized()
    config.onStop()
    super.destroy(context)
  }
}
