package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.OphUrlProperties
import com.github.kagkarlsson.scheduler.Scheduler
import fi.vm.sade.ataru.{AtaruService, AtaruServiceComponent}
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.{OppijanumerorekisteriComponent, OppijanumerorekisteriService}
import fi.vm.sade.hakemuseditori.tarjonta.vanha.RemoteTarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.{TarjontaComponent, TarjontaService}
import fi.vm.sade.hakemuseditori.valintatulokset._
import fi.vm.sade.hakemuseditori.viestintapalvelu.{TuloskirjeComponent, TuloskirjeService}
import fi.vm.sade.hakemuseditori.HakemusEditoriComponent
import fi.vm.sade.omatsivut.config.AppConfig._
import fi.vm.sade.omatsivut.db.impl.OmatsivutDb
import cats.effect.IO
import fi.vm.sade.omatsivut.cas.CasClient
import fi.vm.sade.omatsivut.util.{BlazeHttpClient, ThreadPools}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client

import fi.vm.sade.omatsivut.localization.OmatSivutTranslations
import fi.vm.sade.omatsivut.oppijantunnistus.{OppijanTunnistusComponent, OppijanTunnistusService, RemoteOppijanTunnistusService, StubbedOppijanTunnistusService}
import fi.vm.sade.omatsivut.security._
import fi.vm.sade.omatsivut.security.fake.{FakeCasClient, FakeSecuredSessionServletContainer}
import fi.vm.sade.omatsivut.servlet._
import fi.vm.sade.omatsivut.servlet.session.{LogoutServletContainer, SecuredSessionServletContainer, SessionServlet}
import fi.vm.sade.omatsivut.vastaanotto.VastaanottoComponent

import scala.collection.JavaConverters._

class ComponentRegistry(val config: AppConfig)
  extends TranslationsComponent with
          OhjausparametritComponent with
          ValintatulosServiceComponent with
          HakemusEditoriComponent with
          AtaruServiceComponent with
          OppijanumerorekisteriComponent with
          VastaanottoComponent with
          ApplicationsServletContainer with
          MuistilistaServletContainer with
          SecuredSessionServletContainer with
          FakeSecuredSessionServletContainer with
          LogoutServletContainer with
          RemoteTarjontaComponent with
          TarjontaComponent with
          TuloskirjeComponent with
          OppijanTunnistusComponent with
          TuloskirjeetServletContainer with
          ValintatulosServletContainer with
          NonSensitiveApplicationServletContainer {

  private def configureOhjausparametritService: OhjausparametritService = config match {
    case _: StubbedExternalDeps => new StubbedOhjausparametritService()
    case _ => CachedRemoteOhjausparametritService(OphUrlProperties.url("ohjausparametrit-service.kaikki"))
  }

  private def configureValintatulosService: ValintatulosService = config match {
    case x: StubbedExternalDeps => new FailingRemoteValintatulosService()
    case _ => new RemoteValintatulosService()
  }

  private def configureTarjontaService: TarjontaService = config match {
    case _: StubbedExternalDeps => new StubbedTarjontaService(config)
    case _ => CachedRemoteTarjontaService(config)
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

  private def configureAtaruService: AtaruService = config match {
    case _: StubbedExternalDeps => new StubbedAtaruService
    case _ => new RemoteAtaruService(config)
  }

  private def configureOppijanumerorekisteriService: OppijanumerorekisteriService = config match {
    case _: StubbedExternalDeps => new StubbedOppijanumerorekisteriService
    case _ => new RemoteOppijanumerorekisteriService(config)
  }

  private def configureAuthenticationInfoService: AuthenticationInfoService = config match {
    case _: StubbedExternalDeps => new StubbedAuthenticationInfoService
    case _ => new RemoteAuthenticationInfoService(config.settings.authenticationServiceConfig,
                                                  casOppijaClient,
                                                  config.settings.securitySettings)
  }

  private def configureCASOppijaClient: CasClient = {
    new CasClient(config.settings.securitySettings.casOppijaUrl, persistentHttpClient, AppConfig.callerId)
  }

  private def configureFakeCasOppijaClient: FakeCasClient = config match {
    case _ =>
      val client: Client[IO] = BlazeHttpClient.getClient
      new FakeCasClient(config.settings.securitySettings.casOppijaUrl,
        client,
        AppConfig.callerId,
        authenticationInfoService
      )
  }
//  if (config.isInstanceOf[IT]) {
//    new ApplicationFixtureImporter(springContext).applyFixtures()
//  }
  lazy val persistentHttpClient: Client[IO] = BlazeClientBuilder[IO](ThreadPools.httpExecutionContext).resource.allocated.unsafeRunSync()(ThreadPools.ioRuntime)._1
  lazy val casOppijaClient: CasClient = configureCASOppijaClient
  val fakeCasOppijaClient = configureFakeCasOppijaClient
  val ohjausparametritService: OhjausparametritService = configureOhjausparametritService
  val valintatulosService: ValintatulosService = configureValintatulosService
  val tarjontaService: TarjontaService = configureTarjontaService
  val tuloskirjeService: TuloskirjeService = configureTuloskirjeService
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

  def vastaanottoService(implicit language: Language): VastaanottoService = new VastaanottoService(config)
  def newApplicationsServlet = new ApplicationsServlet(config, sessionService)
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
  def newMuistilistaServlet = new MuistilistaServlet(config)
  def newNonSensitiveApplicationServlet = new NonSensitiveApplicationServlet(config)
  def newTuloskirjeetServlet = new TuloskirjeetServlet(config)
  def newClientErrorLoggingServlet = new ClientErrorLoggingServlet(config)

  override val translations = OmatSivutTranslations
}
