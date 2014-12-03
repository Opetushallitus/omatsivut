package fi.vm.sade.omatsivut.config

import java.util.concurrent.Executors

import fi.vm.sade.omatsivut.auditlog.{AuditLogger, AuditLoggerComponent}
import fi.vm.sade.omatsivut.config.AppConfig._
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.hakemus._
import fi.vm.sade.omatsivut.koodisto.{KoodistoService, KoodistoComponent}
import fi.vm.sade.omatsivut.lomake.{LomakeRepository, LomakeRepositoryComponent}
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.servlet.session.{LogoutServletContainer, SecuredSessionServletContainer}
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.tarjonta.{TarjontaComponent, TarjontaService}
import fi.vm.sade.omatsivut.valintatulokset._

class ComponentRegistry(val config: AppConfig)
  extends SpringContextComponent with
          KoulutusInformaatioComponent with
          OhjausparametritComponent with
          LomakeRepositoryComponent with
          HakemusRepositoryComponent with
          ValintatulosServiceComponent with
          AuditLoggerComponent with
          ApplicationValidatorComponent with
          HakemusPreviewGeneratorComponent with
          HakemusConverterComponent with
          ApplicationsServletContainer with
          KoulutusServletContainer with
          SecuredSessionServletContainer with
          LogoutServletContainer with
          FixtureServletContainer with
          KoodistoServletContainer with
          TarjontaComponent with
          KoodistoComponent {

  implicit val swagger = new OmatSivutSwagger

  private def configureOhjausparametritService: OhjausparametritService = config match {
    case _ : StubbedExternalDeps => new StubbedOhjausparametritService()
    case _ => CachedRemoteOhjausparametritService(config)
  }

  private def configureKoulutusInformaatioService: KoulutusInformaatioService = config match {
    case x: StubbedExternalDeps => new StubbedKoulutusInformaatioService
    case _ => CachedKoulutusInformaatioService(config)
  }

  private def configureValintatulosService: ValintatulosService = new RemoteValintatulosService(config.settings.valintaTulosServiceUrl)

  private def configureTarjontaService : TarjontaService = config match {
    case _ : StubbedExternalDeps => new StubbedTarjontaService()
    case _ => CachedRemoteTarjontaService(config)
  }

  private def configureKoodistoService: KoodistoService = config match {
    case _: StubbedExternalDeps => new StubbedKoodistoService
    case _ => new RemoteKoodistoService(config)
  }

  private lazy val runningLogger = new RunnableLogger
  private lazy val pool = Executors.newSingleThreadExecutor
  lazy val springContext = new OmatSivutSpringContext(OmatSivutSpringContext.createApplicationContext(config))
  val koulutusInformaatioService: KoulutusInformaatioService = configureKoulutusInformaatioService
  val ohjausparametritService: OhjausparametritService = configureOhjausparametritService
  val valintatulosService: ValintatulosService = configureValintatulosService
  val auditLogger: AuditLogger = new AuditLoggerFacade(runningLogger)
  val lomakeRepository: LomakeRepository = new RemoteLomakeRepository
  val hakemusRepository: HakemusRepository = new RemoteHakemusRepository
  val hakemusConverter: HakemusConverter = new HakemusConverter
  val tarjontaService: TarjontaService = configureTarjontaService
  val koodistoService: KoodistoService = configureKoodistoService

  def newApplicationValidator: ApplicationValidator = new ApplicationValidator
  def newHakemusPreviewGenerator(language: Language): HakemusPreviewGenerator = new HakemusPreviewGenerator()(language)
  def newApplicationsServlet = new ApplicationsServlet(config)
  def newKoulutusServlet = new KoulutusServlet
  def newSecuredSessionServlet = new SecuredSessionServlet(config)
  def newLogoutServlet = new LogoutServlet(config)
  def newFixtureServlet = new FixtureServlet(config)
  def newSwaggerServlet = new SwaggerServlet
  def newKoodistoServlet = new KoodistoServlet

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
