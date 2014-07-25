package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.omatsivut.security.{AuthenticationContext, ProductionAuthenticationContext, TestAuthenticationContext}
import org.apache.activemq.broker.BrokerService

object AppConfig extends Logging {
  def fromSystemProperty: AppConfig = {
    val profile: String = System.getProperty("omatsivut.profile", "default")
    logger.info("Using omatsivut.profile=" + profile)
    profile match {
      case "default" => new Default
      case "templated" => new LocalTestingWithTemplatedVars
      case "dev" => new Dev
      case "dev-remote-mongo" => new DevWithRemoteMongo
      case "dev-audit-log" => new DevWithAuditLog
      case "it" => new IT
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  class Default extends AppConfig with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class LocalTestingWithTemplatedVars extends AppConfig with TemplatedProps with TestMode {
    def springConfiguration = new OmatSivutSpringContext.Default()
    def templateAttributesFile = System.getProperty("omatsivut.vars")
  }

  class Dev extends AppConfig with ExampleTemplatedProps with MockAuthentication {
    def springConfiguration = new OmatSivutSpringContext.Dev()
    override def properties = super.properties +
      ("mongodb.oppija.uri" -> "mongodb://localhost:27017")
  }

  class DevWithAuditLog extends AppConfig with ExampleTemplatedProps with MockAuthentication  {
    def springConfiguration = new OmatSivutSpringContext.DevWithAuditLog()

    private var activemqOpt: Option[BrokerService] = None

    override def start {
      val activemq = new BrokerService()
      activemq.addConnector("tcp://localhost:61616")
      activemq.start()
      activemqOpt = Some(activemq)
    }

    override def stop {
      activemqOpt.foreach(_.stop)
      activemqOpt = None
    }

    override def properties = super.properties +
      ("mongodb.oppija.uri" -> "mongodb://localhost:27017") +
      ("log.mongo.uri" -> "${mongodb.oppija.uri}") +
      ("activemq.brokerurl" -> "vm://transport")
  }

  class DevWithRemoteMongo extends MockAuthentication with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Dev()
  }

  class IT extends ExampleTemplatedProps with MockAuthentication with StubbedExternalDeps {
    def springConfiguration = new OmatSivutSpringContext.Dev()

    private var mongo: Option[MongoServer] = None

    override def start {
      mongo = EmbeddedMongo.start
      try {
        FixtureImporter()(this).applyFixtures
      } catch {
        case e: Exception =>
          stop
          throw e
      }
    }
    override def stop {
      mongo.foreach(_.stop)
      mongo = None
    }

    override def properties = super.properties +
      ("mongo.db.name" -> "hakulomake") +
      ("mongodb.oppija.uri" -> "mongodb://localhost:28018")
  }

  trait ExternalProps {
    def configFiles = List(
      "../module-install-parent/config/common/omatsivut/omatsivut.properties",
      "./module-install-parent/config/common/omatsivut/omatsivut.properties",
      System.getProperty("user.home") + "/oph-configuration/common.properties", // for server environments
      System.getProperty("user.home") + "/oph-configuration/omatsivut.properties"
    )
  }

  trait ExampleTemplatedProps extends TemplatedProps {
    def templateAttributesFile = "src/main/resources/oph-configuration/dev-vars.yml"
  }

  trait TemplatedProps {
    def configFiles = List(ConfigTemplateProcessor.createPropertyFileForTestingWithTemplate(templateAttributesFile))
    def templateAttributesFile: String
  }

  trait StubbedExternalDeps extends TestMode {
  }

  trait MockAuthentication extends TestMode {
  }

  trait TestMode extends AppConfig {
    override def isTest = true
  }

  trait AppConfig {
    def springConfiguration: OmatSivutConfiguration
    lazy val springContext = new OmatSivutSpringContext(OmatSivutSpringContext.createApplicationContext(this))
    lazy val authContext: AuthenticationContext = if (isTest) new TestAuthenticationContext else new ProductionAuthenticationContext

    def isTest: Boolean = false
    def start {}
    def stop {}
    def withConfig[T](f: (AppConfig => T)): T = {
      start
      try {
        f(this)
      } finally {
        stop
      }
    }

    def configFiles: List[String]
    lazy val mongoTemplate = springContext.mongoTemplate

    val settings = ApplicationSettings.loadSettings(configFiles)

    def properties = settings.toProperties
  }
}

case class RemoteApplicationConfig(url: String, username: String, password: String, path: String, ticketConsumerPath: String)