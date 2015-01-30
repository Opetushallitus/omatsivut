package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.omatsivut.ValintatulosServiceRunner
import fi.vm.sade.omatsivut.security.{AuthenticationContext, ProductionAuthenticationContext, TestAuthenticationContext}
import fi.vm.sade.utils.config.{ConfigTemplateProcessor, ApplicationSettingsLoader}
import fi.vm.sade.utils.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.tcp.PortFromSystemPropertyOrFindFree

object AppConfig extends Logging {
  private implicit val settingsParser = ApplicationSettingsParser
  val embeddedmongoPortChooser = new PortFromSystemPropertyOrFindFree("omatsivut.embeddedmongo.port")

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
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  class Default extends AppConfig with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Default()
    override def usesFakeAuthentication = settings.environment.isLuokka || settings.environment.isKoulutus
  }

  class LocalTestingWithTemplatedVars(val templateAttributesFile: String = System.getProperty("omatsivut.vars")) extends AppConfig with TemplatedProps with TestMode {
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class Dev extends AppConfig with ExampleTemplatedProps with MockAuthentication {
    def springConfiguration = new OmatSivutSpringContext.Dev()

    override lazy val settings = ConfigTemplateProcessor.createSettings("omatsivut", templateAttributesFile)
      .withOverride("mongodb.oppija.uri", "mongodb://localhost:27017")
  }

  class IT extends ExampleTemplatedProps with MockAuthentication with StubbedExternalDeps {
    def springConfiguration = new OmatSivutSpringContext.Dev()
    override def usesLocalDatabase = true

    private var mongo: Option[MongoServer] = None

    override def onStart {
      mongo = EmbeddedMongo.start(embeddedmongoPortChooser)
      ValintatulosServiceRunner.start
    }

    override def onStop {
      mongo.foreach(_.stop)
      mongo = None
    }

    override lazy val settings = ConfigTemplateProcessor.createSettings("omatsivut", templateAttributesFile)
      .withOverride("omatsivut.valinta-tulos-service.url", "http://localhost:"+ ValintatulosServiceRunner.valintatulosPortChooser.chosenPort+"/valinta-tulos-service")
      .withOverride("mongo.db.name", "hakulomake")
      .withOverride("mongodb.oppija.uri", "mongodb://localhost:" + embeddedmongoPortChooser.chosenPort)
  }

  class ImmediateCookieTimeout extends IT {
    override val cookieTimeoutMinutes = 0
  }

  trait ExternalProps {
    def configFile = System.getProperty("user.home") + "/oph-configuration/omatsivut.properties"
    lazy val settings = ApplicationSettingsLoader.loadSettings(configFile)
  }

  trait ExampleTemplatedProps extends AppConfig with TemplatedProps {
    def templateAttributesFile = "src/main/resources/oph-configuration/dev-vars.yml"
    override def usesLocalDatabase = true
  }

  trait TemplatedProps {
    logger.info("Using template variables from " + templateAttributesFile)
    lazy val settings = ConfigTemplateProcessor.createSettings("omatsivut", templateAttributesFile)
    def templateAttributesFile: String
  }

  trait StubbedExternalDeps extends TestMode {
  }

  trait MockAuthentication extends TestMode {
  }

  trait TestMode extends AppConfig {
    override def usesFakeAuthentication = true
  }

  trait AppConfig {
    def springConfiguration: OmatSivutConfiguration
    lazy val authContext: AuthenticationContext = if (usesFakeAuthentication) new TestAuthenticationContext else new ProductionAuthenticationContext
    val cookieTimeoutMinutes = 30

    def usesFakeAuthentication: Boolean = false
    def usesLocalDatabase = false

    def onStart {}
    def onStop {}

    def settings: ApplicationSettings
  }
}

case class RemoteApplicationConfig(url: String, username: String, password: String, ticketConsumerPath: String, config: Config)