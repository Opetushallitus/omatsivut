package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.OphUrlProperties
import com.github.kagkarlsson.scheduler.Scheduler
import fi.vm.sade.ataru.{AtaruService, AtaruServiceComponent}
import fi.vm.sade.groupemailer.{GroupEmailComponent, GroupEmailService}
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus._
import fi.vm.sade.hakemuseditori.hakumaksu.{HakumaksuServiceWrapper, RemoteHakumaksuServiceWrapper, StubbedHakumaksuServiceWrapper}
import fi.vm.sade.hakemuseditori.koodisto.{KoodistoComponent, KoodistoService, RemoteKoodistoService, StubbedKoodistoService}
import fi.vm.sade.hakemuseditori.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.lomake.{LomakeRepository, LomakeRepositoryComponent}
import fi.vm.sade.hakemuseditori.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.{OppijanumerorekisteriComponent, OppijanumerorekisteriService}
import fi.vm.sade.hakemuseditori.tarjonta.vanha.RemoteTarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.{TarjontaComponent, TarjontaService}
import fi.vm.sade.hakemuseditori.valintatulokset._
import fi.vm.sade.hakemuseditori.viestintapalvelu.{TuloskirjeComponent, TuloskirjeService}
import fi.vm.sade.hakemuseditori.{HakemusEditoriComponent, RemoteSendMailServiceWrapper, SendMailServiceWrapper, StubbedSendMailServiceWrapper}
import fi.vm.sade.omatsivut.config.AppConfig._
import fi.vm.sade.omatsivut.db.impl.OmatsivutDb
//import fi.vm.sade.omatsivut.fixtures.hakemus.ApplicationFixtureImporter
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.localization.OmatSivutTranslations
import fi.vm.sade.omatsivut.muistilista.MuistilistaServiceComponent
import fi.vm.sade.omatsivut.oppijantunnistus.{OppijanTunnistusComponent, OppijanTunnistusService, RemoteOppijanTunnistusService, StubbedOppijanTunnistusService}
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.security.fake.{FakeCasClient, FakeSecuredSessionServletContainer}
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.servlet.session.{LogoutServletContainer, SecuredSessionServletContainer, SessionServlet}
import fi.vm.sade.omatsivut.vastaanotto.VastaanottoComponent
import fi.vm.sade.utils.captcha.CaptchaServiceComponent
import fi.vm.sade.utils.cas.CasClient
import org.http4s.client.blaze

import scala.collection.JavaConverters._

class ComponentRegistry(val config: AppConfig)
  extends SpringContextComponent with
          TranslationsComponent with
          MuistilistaServiceComponent with
          GroupEmailComponent with
          KoulutusInformaatioComponent with
          OhjausparametritComponent with
          LomakeRepositoryComponent with
          HakemusRepositoryComponent with
          ValintatulosServiceComponent with
          ApplicationValidatorComponent with
          HakemusPreviewGeneratorComponent with
          HakemusConverterComponent with
          HakemusEditoriComponent with
          AtaruServiceComponent with
          OppijanumerorekisteriComponent with
          VastaanottoComponent with
          ApplicationsServletContainer with
          MuistilistaServletContainer with
          CaptchaServiceComponent with
          KoulutusServletContainer with
          SecuredSessionServletContainer with
          FakeSecuredSessionServletContainer with
          LogoutServletContainer with
          FixtureServletContainer with
          KoodistoServletContainer with
          RemoteTarjontaComponent with
          TarjontaComponent with
          TuloskirjeComponent with
          KoodistoComponent with
          OppijanTunnistusComponent with
          TuloskirjeetServletContainer with
          ValintatulosServletContainer with
          NonSensitiveApplicationServletContainer {

  private def configureOhjausparametritService: OhjausparametritService = config match {
    case _: StubbedExternalDeps => new StubbedOhjausparametritService()
    case _ => CachedRemoteOhjausparametritService(OphUrlProperties.url("ohjausparametrit-service.kaikki"))
  }

  private def configureKoulutusInformaatioService: KoulutusInformaatioService = config match {
    case x: StubbedExternalDeps => new StubbedKoulutusInformaatioService
    case _ => CachedKoulutusInformaatioService(new RemoteKoulutusService())
  }

  private def configureGroupEmailService: GroupEmailService = config match {
    case x: StubbedExternalDeps => new FakeGroupEmailService
    case _ => new RemoteGroupEmailService(config.settings, AppConfig.callerId)
  }

  private def configureValintatulosService: ValintatulosService = config match {
    case x: StubbedExternalDeps => new FailingRemoteValintatulosService()
    case _ => new RemoteValintatulosService()
  }

  private def configureTarjontaService: TarjontaService = config match {
    case _: StubbedExternalDeps => new StubbedTarjontaService(config)
    case _ => CachedRemoteTarjontaService(config, casVirkailijaClient)
  }

  private def configureTuloskirjeService: TuloskirjeService = config match {
    case _: StubbedExternalDeps => new StubbedTuloskirjeService()
    case _: Cloud => new S3TulosKirjeService(config)
    case _ => new SharedDirTuloskirjeService(config)
  }

  private def configureOppijanTunnistusService: OppijanTunnistusService = config match {
    case _: StubbedExternalDeps => new StubbedOppijanTunnistusService()
    case _ => new RemoteOppijanTunnistusService(RemoteOppijanTunnistusService.createCasClient(config))
  }

  private def configureKoodistoService: KoodistoService = config match {
    case _: StubbedExternalDeps => new StubbedKoodistoService
    case _ => new RemoteKoodistoService(springContext, AppConfig.callerId)
  }

  private def configureHakumaksuService: HakumaksuServiceWrapper = config match {
    case _: StubbedExternalDeps => new StubbedHakumaksuServiceWrapper
    case _ => new RemoteHakumaksuServiceWrapper(springContext)
  }

  private def configureSendMailService: SendMailServiceWrapper = config match {
    case _: StubbedExternalDeps => new StubbedSendMailServiceWrapper
    case _ => new RemoteSendMailServiceWrapper(springContext)
  }

  private def configureAtaruService: AtaruService = config match {
    case _: StubbedExternalDeps => new StubbedAtaruService
    case _ => new RemoteAtaruService(config, casVirkailijaClient)
  }

  private def configureOppijanumerorekisteriService: OppijanumerorekisteriService = config match {
    case _: StubbedExternalDeps => new StubbedOppijanumerorekisteriService
    case _ => new RemoteOppijanumerorekisteriService(config, casVirkailijaClient)
  }

  private def configureAuthenticationInfoService: AuthenticationInfoService = config match {
    case _: StubbedExternalDeps => new StubbedAuthenticationInfoService
    case _ => new RemoteAuthenticationInfoService(config.settings.authenticationServiceConfig,
                                                  casOppijaClient,
                                                  config.settings.securitySettings)
  }

  private def configureCASVirkailijaClient: CasClient = config match {
    case _ => new CasClient(config.settings.securitySettings.casVirkailijaUrl,
                            blaze.defaultClient,
                            AppConfig.callerId)
  }

  private def configureCASOppijaClient: CasClient = config match {
    case _ => new CasClient(config.settings.securitySettings.casOppijaUrl,
                            blaze.defaultClient,
                            AppConfig.callerId)
  }

  private def configureFakeCasOppijaClient: FakeCasClient = config match {
    case _ =>
      new FakeCasClient(config.settings.securitySettings.casOppijaUrl,
        blaze.defaultClient,
        AppConfig.callerId,
        authenticationInfoService
      )
  }

  lazy val springContext = new HakemusSpringContext(OmatSivutSpringContext.createApplicationContext(config))
//  if (config.isInstanceOf[IT]) {
//    new ApplicationFixtureImporter(springContext).applyFixtures()
//  }

  val casVirkailijaClient: CasClient = configureCASVirkailijaClient
  val casOppijaClient = configureCASOppijaClient
  val fakeCasOppijaClient = configureFakeCasOppijaClient

  val hakumaksuService: HakumaksuServiceWrapper = configureHakumaksuService
  val sendMailService: SendMailServiceWrapper = configureSendMailService
  val koulutusInformaatioService: KoulutusInformaatioService = configureKoulutusInformaatioService
  val ohjausparametritService: OhjausparametritService = configureOhjausparametritService
  val valintatulosService: ValintatulosService = configureValintatulosService
  val lomakeRepository: LomakeRepository = new RemoteLomakeRepository
  val hakemusConverter: HakemusConverter = new HakemusConverter
  val tarjontaService: TarjontaService = configureTarjontaService
  val tuloskirjeService: TuloskirjeService = configureTuloskirjeService
  val koodistoService: KoodistoService = configureKoodistoService
  val groupEmailService: GroupEmailService = configureGroupEmailService
  val captchaService: CaptchaService = new RemoteCaptchaService(config.settings.captchaSettings)
  val oppijanTunnistusService = configureOppijanTunnistusService
  val ataruService: AtaruService = configureAtaruService
  val oppijanumerorekisteriService: OppijanumerorekisteriService = configureOppijanumerorekisteriService
  val omatsivutDb = new OmatsivutDb(config.settings.omatsivutDbConfig,
    config.settings.sessionTimeoutSeconds.getOrElse(3600))
  implicit val sessionService = new SessionService(omatsivutDb)
  lazy val authenticationInfoService = configureAuthenticationInfoService

  private def configureScheduler() = {
    val numberOfThreads: Int = 1
    val scheduledTasks = List(
      SessionCleaner.createTaskForScheduler(sessionService, config.settings.sessionCleanupCronString.getOrElse("0 10 0 * * ?"))
    )
    val scheduler: Scheduler = Scheduler.create(omatsivutDb.dataSource).startTasks(scheduledTasks.asJava).threads(numberOfThreads).build
    logger.info(s"Starting scheduler with ${scheduledTasks.length} task(s)")
    scheduler.start()
    scheduler
  }

  val scheduler: Scheduler = configureScheduler()

  def muistilistaService(language: Language): MuistilistaService = new MuistilistaService(language)
  def vastaanottoService(implicit language: Language): VastaanottoService = new VastaanottoService()
  def newApplicationValidator: ApplicationValidator = new ApplicationValidator
  def newHakemusPreviewGenerator(language: Language): HakemusPreviewGenerator = new HakemusPreviewGenerator(language)
  def newApplicationsServlet = new ApplicationsServlet(config, sessionService)
  def newKoulutusServlet = new KoulutusServlet
  def newValintatulosServlet = new ValintatulosServlet(config, sessionService)
  def newSecuredSessionServlet = config match {
    case _: StubbedExternalDeps => new FakeSecuredSessionServlet(config,
      authenticationInfoService,
      sessionService,
      config.settings.sessionTimeoutSeconds,
      fakeCasOppijaClient)
    case _ => new SecuredSessionServlet(config,
      authenticationInfoService,
      sessionService,
      config.settings.sessionTimeoutSeconds,
      casOppijaClient)
  }
  def newSessionServlet = new SessionServlet()
  def newLogoutServlet = new LogoutServlet()
  def newFixtureServlet = new FixtureServlet(config)
  def newKoodistoServlet = new KoodistoServlet
  def newMuistilistaServlet = new MuistilistaServlet(config)
  def newNonSensitiveApplicationServlet = new NonSensitiveApplicationServlet(config)
  def newTuloskirjeetServlet = new TuloskirjeetServlet(config)
  def newClientErrorLoggingServlet = new ClientErrorLoggingServlet(config)

  override val translations = OmatSivutTranslations
}
