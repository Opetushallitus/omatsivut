package fi.vm.sade.omatsivut.config

import java.util.concurrent.Executors

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, AuditLoggerComponent}
import fi.vm.sade.omatsivut.config.AppConfig.{MockAuthentication, AppConfig, ITWithValintaTulosService, StubbedExternalDeps}
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.hakemus._
import fi.vm.sade.omatsivut.haku.{HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.security.{AuthenticationInfoComponent, AuthenticationInfoService}
import fi.vm.sade.omatsivut.servlet.session.{LogoutServletContainer, SecuredSessionServletContainer}
import fi.vm.sade.omatsivut.servlet.{SwaggerServlet, OmatSivutSwagger, KoulutusServletContainer, ApplicationsServletContainer}
import fi.vm.sade.omatsivut.valintatulokset._

protected class ComponentRegistry(val config: AppConfig)
  extends SpringContextComponent with
          KoulutusInformaatioComponent with
          OhjausparametritComponent with
          HakuRepositoryComponent with
          HakemusRepositoryComponent with
          ValintatulosServiceComponent with
          AuditLoggerComponent with
          AuthenticationInfoComponent with
          ApplicationValidatorComponent with
          HakemusPreviewGeneratorComponent with
          HakemusConverterComponent with
          ApplicationsServletContainer with
          KoulutusServletContainer with
          SecuredSessionServletContainer with
          LogoutServletContainer {

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
    case _ => new RemoteAuthenticationInfoService(config.settings.authenticationServiceConfig, config)
  }

  lazy val springContext: OmatSivutSpringContext = config.springContext
  private lazy val runningLogger = new RunnableLogger
  private lazy val pool = Executors.newSingleThreadExecutor
  val koulutusInformaatioService: KoulutusInformaatioService = configureKoulutusInformaatioService
  val ohjausparametritService: OhjausparametritService = configureOhjausparametritService
  val valintatulosService: ValintatulosService = configureValintatulosService
  val authenticationInfoService: AuthenticationInfoService = configureAuthenticationInfoService
  val auditLogger: AuditLogger = new AuditLoggerFacade(runningLogger)
  val hakuRepository: HakuRepository = new RemoteHakuRepository
  val hakemusRepository: HakemusRepository = new RemoteHakemusRepository
  val hakemusConverter: HakemusConverter = new HakemusConverter

  def newApplicationValidator: ApplicationValidator = new ApplicationValidator
  def newHakemusPreviewGenerator(language: Language): HakemusPreviewGenerator = new HakemusPreviewGenerator()(language)
  def newApplicationsServlet = new ApplicationsServlet(config)
  def newKoulutusServlet = new KoulutusServlet
  def newSecuredSessionServlet = new SecuredSessionServlet(config)
  def newLogoutServlet = new LogoutServlet(config)
  def newSwaggerServlet = new SwaggerServlet

  def start {
    pool.execute(runningLogger)
  }

  def stop {

  }

}
