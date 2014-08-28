package fi.vm.sade.omatsivut.config

import java.util.concurrent.Executors

import com.typesafe.config.Config
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.auditlog.RunnableLogger
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.omatsivut.security.{AuthenticationContext, ProductionAuthenticationContext, TestAuthenticationContext}
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
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  class Default extends AppConfig with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Default()
    override def usesFakeAuthentication = if (settings.environment == "ophitest") true else false
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

    override def properties = super.properties +
      ("mongodb.oppija.uri" -> "mongodb://localhost:27017") +
      ("log.mongo.uri" -> "${mongodb.oppija.uri}") +
      ("activemq.brokerurl" -> "vm://transport")
  }

  class IT extends ExampleTemplatedProps with MockAuthentication with StubbedExternalDeps {
    def springConfiguration = new OmatSivutSpringContext.Dev()
    override def usesLocalDatabase = true

    private var mongo: Option[MongoServer] = None

    override def onStart {
      mongo = EmbeddedMongo.start
      try {
        FixtureImporter()(this).applyFixtures()
      } catch {
        case e: Exception =>
          stop
          throw e
      }
    }
    override def onStop {
      mongo.foreach(_.stop)
      mongo = None
    }

    override def properties = super.properties +
      ("mongo.db.name" -> "hakulomake") +
      ("mongodb.oppija.uri" -> "mongodb://localhost:28018")
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
    lazy val springContext = new OmatSivutSpringContext(OmatSivutSpringContext.createApplicationContext(this))
    lazy val authContext: AuthenticationContext = if (usesFakeAuthentication) new TestAuthenticationContext else new ProductionAuthenticationContext
    lazy val auditLogger = new RunnableLogger(this)
    private lazy val pool = Executors.newSingleThreadExecutor()

    def usesFakeAuthentication: Boolean = false
    def usesLocalDatabase = false
    final def start {
      pool.execute(auditLogger)
      onStart
    }
    final def stop {
      pool.shutdown()
      onStop
    }
    def onStart {}
    def onStop {}
    def withConfig[T](f: (AppConfig => T)): T = {
      start
      try {
        f(this)
      } finally {
        stop
      }
    }


    lazy val mongoTemplate = springContext.mongoTemplate

    def settings: ApplicationSettings

    def properties = settings.toProperties
  }
}

case class RemoteApplicationConfig(url: String, username: String, password: String, ticketConsumerPath: String, config: Config)