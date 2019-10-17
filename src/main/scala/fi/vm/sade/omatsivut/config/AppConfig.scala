package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.config.{ApplicationSettingsLoader, ConfigTemplateProcessor}
import fi.vm.sade.utils.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.{PortFromSystemPropertyOrFindFree}

object AppConfig extends Logging {

  val callerId = "1.2.246.562.10.00000000001.omatsivut.backend"

  private implicit val settingsParser = ApplicationSettingsParser
  val embeddedMongoPortChooser = new PortFromSystemPropertyOrFindFree("omatsivut.embeddedmongo.port")

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
      .withOverride("mongodb.oppija.uri", "mongodb://localhost:" + embeddedMongoPortChooser.chosenPort)
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
    OphUrlProperties.addOverride("shibboleth_logout", "/Shibboleth.sso/Logout?return=$1")
    OphUrlProperties.addOverride("shibboleth_login", "/Shibboleth.sso/Login$1")

    val embeddedMongoService = new EmbeddedMongoService
    val localPostgresService = new LocalPostgresService

    override lazy val settings = ConfigTemplateProcessor.createSettings("common", templateAttributesFile)
      .withOverride("omatsivut.valinta-tulos-service.url", "http://localhost:"+ embeddedJettyPortChooser.chosenPort + "/valinta-tulos-service")
      .withOverride("mongo.db.name", "hakulomake")
      .withOverride("mongodb.oppija.uri", "mongodb://localhost:" + embeddedMongoPortChooser.chosenPort)
      .withOverride("omatsivut.db.port", itPostgresPortChooser.chosenPort.toString)
      .withOverride("omatsivut.db.host", "localhost")
      .withOverride("omatsivut.db.url", "jdbc:postgresql://localhost:" + itPostgresPortChooser.chosenPort + "/omatsivutdb")

    override def onStart: Unit = {
      embeddedMongoService.start()
      localPostgresService.start()
    }

    override def onStop: Unit = {
      try {
        embeddedMongoService.stop()
      } catch {
        case e: Throwable => logger.info("Failed to stop embedded mongo ", e)
      }
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

  class EmbeddedMongoService extends LocalService {
    private var mongo: Option[MongoServer] = None

    override def start() {
      mongo = EmbeddedMongo.start(embeddedMongoPortChooser)
    }

    override def stop() {
      mongo.foreach(_.stop)
      mongo = None
    }
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
