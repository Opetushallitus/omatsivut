package fi.vm.sade.omatsivut.config

import java.util.concurrent.Executors

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, AuditLoggerComponent}
import fi.vm.sade.omatsivut.config.AppConfig.{MockAuthentication, AppConfig, ITWithValintaTulosService, StubbedExternalDeps}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.hakemus.{HakemusRepository, HakemusRepositoryComponent}
import fi.vm.sade.omatsivut.haku.{HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.security.{AuthenticationInfoComponent, AuthenticationInfoService}
import fi.vm.sade.omatsivut.servlet.session.{LogoutServletComponent, SecuredSessionServletComponent}
import fi.vm.sade.omatsivut.servlet.{SwaggerServlet, OmatSivutSwagger, KoulutusServletComponent, ApplicationsServletContainer}
import fi.vm.sade.omatsivut.valintatulokset._

protected class ComponentRegistry(implicit val config: AppConfig)
  extends KoulutusInformaatioComponent with
          OhjausparametritComponent with
          HakuRepositoryComponent with
          HakemusRepositoryComponent with
          ValintatulosServiceComponent with
          AuditLoggerComponent with
          AuthenticationInfoComponent with
          ApplicationsServletContainer with
          KoulutusServletComponent with
          SecuredSessionServletComponent with
          LogoutServletComponent {

  implicit val swagger = new OmatSivutSwagger

  private def configureOhjausparametritService: OhjausparametritService = config match {
    case _ : StubbedExternalDeps => new StubbedOhjausparametritService()
    case _ => CachedRemoteOhjausparametritService(config)
  }

  private def configureKoulutusInformaatioService: KoulutusInformaatioService = config match {
    case x: StubbedExternalDeps => new StubbedKoulutusInformaatioService
    case _ => CachedKoulutusInformaatioService(config)
  }

  private def configureValintatulosService: ValintatulosService = config match {
    case x: ITWithValintaTulosService =>
      new RemoteValintatulosService(config.settings.valintaTulosServiceUrl)
    case x: StubbedExternalDeps =>
      new MockValintatulosService()
    case _ if config.settings.environment.isProduction || config.settings.environment.isQA =>
      new NoOpValintatulosService
    case _ =>
      new RemoteValintatulosService(config.settings.valintaTulosServiceUrl)
  }

  //RemoteValintatulosService(appConfig.settings.sijoitteluServiceConfig.url)

  private def configureAuthenticationInfoService: AuthenticationInfoService = config match {
    case x: MockAuthentication => new AuthenticationInfoService {
      def getHenkiloOID(hetu: String) = TestFixture.persons.get(hetu)
    }
    case _ => new RemoteAuthenticationInfoService(config.settings.authenticationServiceConfig)(config)
  }

  lazy val springContext: OmatSivutSpringContext = config.springContext
  private lazy val runningLogger = new RunnableLogger(config)
  private lazy val pool = Executors.newSingleThreadExecutor()
  val koulutusInformaatioService: KoulutusInformaatioService = configureKoulutusInformaatioService
  val ohjausparametritService: OhjausparametritService = configureOhjausparametritService
  val valintatulosService: ValintatulosService = configureValintatulosService
  val authenticationInfoService: AuthenticationInfoService = configureAuthenticationInfoService
  val auditLogger: AuditLogger = new AuditLoggerFacade(runningLogger)
  val hakuRepository: HakuRepository = new RemoteHakuRepository()
  val hakemusRepository: HakemusRepository = new RemoteHakemusRepository()

  def newApplicationsServlet = new ApplicationsServlet()
  def newKoulutusServlet = new KoulutusServlet()
  def newSecuredSessionServlet = new SecuredSessionServlet()
  def newLogoutServlet = new LogoutServlet()
  def newSwaggerServlet = new SwaggerServlet()

  def start {
    pool.execute(runningLogger)
  }

  def stop {

  }
}
