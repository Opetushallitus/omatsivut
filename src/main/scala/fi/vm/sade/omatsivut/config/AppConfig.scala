package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.util.{ApplicationSettingsLoader, ConfigTemplateProcessor, Logging, PortFromSystemPropertyOrFindFree}

object AppConfig extends Logging {

  val callerId = "1.2.246.562.10.00000000001.omatsivut.backend"

  val suomifi_valtuudet_enabled = false

  private implicit val settingsParser = ApplicationSettingsParser

  val itPostgresPortChooser = new PortFromSystemPropertyOrFindFree("omatsivut.it.postgres.port")

  val embeddedJettyPortChooser = new PortFromSystemPropertyOrFindFree("omatsivut.port")

  def getProfileProperty() = System.getProperty("omatsivut.profile", "default")

  def fromSystemProperty: AppConfig = {
    fromString(getProfileProperty)
  }

  def fromOptionalString(profile: Option[String]) = {
    fromString(profile.getOrElse(getProfileProperty))
  }

  def fromString(profile: String): AppConfig = {
    logger.info("Using omatsivut.profile=" + profile)
    profile match {
      case "default" => new Default
      case "templated" => new LocalTestingWithTemplatedVars
      case "dev" => new Dev
      case "it" => new IT
      case "cloud" => new Cloud
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  class Default extends AppConfig with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class Cloud extends AppConfig with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class LocalTestingWithTemplatedVars(val templateAttributesFile: String = System.getProperty("omatsivut.vars")) extends AppConfig with TemplatedProps with MockAuthentication {
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class Dev extends AppConfig with ExampleTemplatedProps with MockAuthentication with StubbedExternalDeps {
    val localPostgresService = new LocalPostgresService

    def springConfiguration = new OmatSivutSpringContext.Dev()

    override lazy val settings = ConfigTemplateProcessor.createSettings("common", templateAttributesFile)
      .withOverride("omatsivut.db.port", itPostgresPortChooser.chosenPort.toString)
      .withOverride("omatsivut.db.host", "localhost")
      .withOverride("omatsivut.db.url", "jdbc:postgresql://localhost:" + itPostgresPortChooser.chosenPort + "/omatsivutdb")

    override def onStart: Unit = {
      localPostgresService.start()
    }

    override def onStop: Unit = {
      localPostgresService.stop()
    }
  }

  class IT extends AppConfig with ExampleTemplatedProps with MockAuthentication with StubbedExternalDeps {
    def springConfiguration: OmatSivutConfiguration = new OmatSivutSpringContext.Dev()

    // Testien vaatimat overridet
    OphUrlProperties.addOverride("url-oppija", "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort.toString)
    OphUrlProperties.addOverride("url-virkailija", "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort.toString)
    OphUrlProperties.addOverride("vetuma.url", "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort.toString)
    OphUrlProperties.addOverride("host.haku", "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort.toString)
    OphUrlProperties.addOverride("host.haku.sv", "http://localhost:" + AppConfig.embeddedJettyPortChooser.chosenPort.toString)
    OphUrlProperties.addOverride("protocol_ataru_hakija", "http")
    OphUrlProperties.addOverride("host_ataru_hakija", "localhost:8351")
    OphUrlProperties.addOverride("cas.oppija.login", "/cas-oppija/login?locale=$1&valtuudet=$2&service=$3")
    OphUrlProperties.addOverride("cas.oppija.logout", "/cas-oppija/logout?service=$1")


    val localPostgresService = new LocalPostgresService

    override lazy val settings = ConfigTemplateProcessor.createSettings("common", templateAttributesFile)
      .withOverride("omatsivut.valinta-tulos-service.url", "http://localhost:"+ embeddedJettyPortChooser.chosenPort + "/valinta-tulos-service")
      .withOverride("omatsivut.db.port", itPostgresPortChooser.chosenPort.toString)
      .withOverride("omatsivut.db.host", "localhost")
      .withOverride("omatsivut.db.url", "jdbc:postgresql://localhost:" + itPostgresPortChooser.chosenPort + "/omatsivut")
      .withOverride("valinta-tulos-service.valintarekisteri.db.user", "oph")
      .withOverride("valinta-tulos-service.valintarekisteri.db.password", "oph")

    override def onStart: Unit = {
      localPostgresService.start()
    }

    override def onStop: Unit = {
      localPostgresService.stop()
    }

  }

  class ImmediateCookieTimeout extends IT {
    override val cookieTimeoutMinutes = 0
  }

  trait ExternalProps {
    def configFile = System.getProperty("user.home") + "/oph-configuration/common.properties"
    lazy val settings = ApplicationSettingsLoader.loadSettings(configFile)
  }

  trait ExampleTemplatedProps extends AppConfig with TemplatedProps {
    def templateAttributesFile = "src/main/resources/oph-configuration/dev-vars.yml"
    override def usesLocalDatabase = true
  }

  trait TemplatedProps {
    logger.info("Using template variables from " + templateAttributesFile)
    lazy val settings = ConfigTemplateProcessor.createSettings("common", templateAttributesFile)
    def templateAttributesFile: String
  }

  trait StubbedExternalDeps {
  }

  trait LocalService {
    def start() {}
    def stop() {}
  }

  class LocalPostgresService extends LocalService {
    private val itPostgres = new ITPostgres(itPostgresPortChooser)

    override def start() {
      itPostgres.start()
    }

    override def stop(): Unit = {
      itPostgres.stop()
    }
  }

  trait MockAuthentication extends AppConfig {
  }

  trait AppConfig {
    def springConfiguration: OmatSivutConfiguration
    val cookieTimeoutMinutes = 30

    def usesLocalDatabase = false

    def onStart() {}
    def onStop() {}

    def settings: ApplicationSettings
  }
}

case class RemoteApplicationConfig(url: String, username: String, password: String, ticketConsumerPath: String, config: Config)
