package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.omatsivut.ValintatulosServiceRunner
import fi.vm.sade.omatsivut.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.omatsivut.security.{AuthenticationContext, ProductionAuthenticationContext, TestAuthenticationContext}
import fi.vm.sade.omatsivut.util.Logging
import org.apache.activemq.broker.BrokerService

object AppConfig extends Logging {

  def getProfileProperty() = System.getProperty("omatsivut.profile", "default")

  def fromSystemProperty: AppConfig = {
    val profile: String = getProfileProperty
    logger.info("Using omatsivut.profile=" + profile)
    profile match {
      case "default" => new Default
      case "templated" => new LocalTestingWithTemplatedVars
      case "dev" => new Dev
      case "dev-audit-log" => new DevWithAuditLog
      case "it" => new IT
      case "it-with-valintatulos" => new ITWithValintaTulosService
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  class Default extends AppConfig with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Default()
    override def usesFakeAuthentication = settings.environment.isLuokka
  }

  class LocalTestingWithTemplatedVars(val templateAttributesFile: String = System.getProperty("omatsivut.vars")) extends AppConfig with TemplatedProps with TestMode {
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class Dev extends AppConfig with ExampleTemplatedProps with MockAuthentication {
    def springConfiguration = new OmatSivutSpringContext.Dev()

    override lazy val settings = ConfigTemplateProcessor.createSettings(templateAttributesFile)
      .withOverride("mongodb.oppija.uri", "mongodb://localhost:27017")
  }

  class DevWithAuditLog extends AppConfig with ExampleTemplatedProps with MockAuthentication  {
    def springConfiguration = new OmatSivutSpringContext.DevWithAuditLog()

    private var activemqOpt: Option[BrokerService] = None

    override def onStart {
      val activemq = new BrokerService()
      activemq.addConnector("tcp://localhost:61616")
      activemq.start()
      activemqOpt = Some(activemq)
    }

    override def onStop {
      activemqOpt.foreach(_.stop)
      activemqOpt = None
    }

    override lazy val settings = ConfigTemplateProcessor.createSettings(templateAttributesFile)
      .withOverride("mongodb.oppija.uri", "mongodb://localhost:27017")
      .withOverride("log.mongo.uri", "mongodb://localhost:27017")
      .withOverride("activemq.brokerurl", "vm://transport")
  }

  class IT extends ExampleTemplatedProps with MockAuthentication with StubbedExternalDeps {
    def springConfiguration = new OmatSivutSpringContext.Dev()
    override def usesLocalDatabase = true

    private var mongo: Option[MongoServer] = None

    override def onStart {
      mongo = EmbeddedMongo.start
      ValintatulosServiceRunner.start
    }

    override def onStop {
      mongo.foreach(_.stop)
      mongo = None
    }

    override lazy val settings = ConfigTemplateProcessor.createSettings(templateAttributesFile)
      .withOverride("omatsivut.valinta-tulos-service.url", "http://localhost:"+ ValintatulosServiceRunner.valintatulosPort+"/valinta-tulos-service")
      .withOverride("mongo.db.name", "hakulomake")
      .withOverride("mongodb.oppija.uri", "mongodb://localhost:" + EmbeddedMongo.port)
  }

  class ITWithValintaTulosService extends IT {

  }

  class ImmediateCookieTimeout extends IT {
    override val cookieTimeoutMinutes = 0
  }

  trait ExternalProps {
    def configFile = System.getProperty("user.home") + "/oph-configuration/omatsivut.properties"
    lazy val settings = ApplicationSettings.loadSettings(configFile)
  }

  trait ExampleTemplatedProps extends AppConfig with TemplatedProps {
    def templateAttributesFile = "src/main/resources/oph-configuration/dev-vars.yml"
    override def usesLocalDatabase = true
  }

  trait TemplatedProps {
    logger.info("Using template variables from " + templateAttributesFile)
    lazy val settings = ConfigTemplateProcessor.createSettings(templateAttributesFile)
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

    def properties = settings.toProperties
  }
}

case class RemoteApplicationConfig(url: String, username: String, password: String, ticketConsumerPath: String, config: Config)