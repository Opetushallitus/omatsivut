package fi.vm.sade.omatsivut.config

import java.util.concurrent.Executors

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, AuditLoggerComponent}
import fi.vm.sade.omatsivut.config.AppConfig._
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.hakemus._
import fi.vm.sade.omatsivut.haku.{HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.security.{AuthenticationInfoComponent, AuthenticationInfoService}
import fi.vm.sade.omatsivut.servlet.session.{LogoutServletContainer, SecuredSessionServletContainer}
import fi.vm.sade.omatsivut.servlet.testing.TestHelperServletContainer
import fi.vm.sade.omatsivut.servlet.{SwaggerServlet, OmatSivutSwagger, KoulutusServletContainer, ApplicationsServletContainer}
import fi.vm.sade.omatsivut.valintatulokset._

class ComponentRegistry(val config: AppConfig)
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
          LogoutServletContainer with
          TestHelperServletContainer {

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
    case _ =>
      new RemoteValintatulosService(config.settings.valintaTulosServiceUrl)
  }

  private def configureAuthenticationInfoService: AuthenticationInfoService = config match {
    case x: MockAuthentication => new AuthenticationInfoService {
      def getHenkiloOID(hetu: String) = TestFixture.persons.get(hetu)
    }
    case _ => new RemoteAuthenticationInfoService(config.settings.authenticationServiceConfig, config)
  }


  private lazy val runningLogger = new RunnableLogger
  private lazy val pool = Executors.newSingleThreadExecutor
  lazy val springContext = new OmatSivutSpringContext(OmatSivutSpringContext.createApplicationContext(config))
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
  def newTestHelperServlet = new TestHelperServlet(config)
  def newSwaggerServlet = new SwaggerServlet

  def start {
    try {
      config.onStart
      pool.execute(runningLogger)
      if(config.isInstanceOf[IT]) {
        new FixtureImporter(springContext.applicationDAO, springContext.mongoTemplate).applyFixtures()
      }
    } catch {
      case e: Exception =>
        stop
        throw e
    }
  }

  def stop {
    config.onStop
  }
}
