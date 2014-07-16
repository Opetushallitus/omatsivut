package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.mongo.{EmbeddedMongo, MongoServer}
import fi.vm.sade.omatsivut.security.{AuthenticationInfoService, RemoteAuthenticationInfoService}

object AppConfig extends Logging {
  def fromSystemProperty: AppConfig = {
    val profile: String = System.getProperty("omatsivut.profile", "default")
    logger.info("Using omatsivut.profile=" + profile)
    profile match {
      case "default" => new Default
      case "dev" => new Dev
      case "dev-remote-mongo" => new DevWithRemoteMongo
      case "it" => new IT
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  class Default extends AppConfig with ExternalProps {
    def authenticationInfoService: AuthenticationInfoService = new RemoteAuthenticationInfoService(settings.authenticationService)(this)
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class Dev extends AppConfig with StubbedExternalDeps with TestMode {
    def springConfiguration = new OmatSivutSpringContext.Dev()
    def configFiles = List("src/main/resources/dev.conf")
  }
  class DevWithRemoteMongo extends StubbedExternalDeps with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Dev()
  }

  class IT extends StubbedExternalDeps with TestMode {
    def springConfiguration = new OmatSivutSpringContext.IT()
    def configFiles = List("src/main/resources/it.conf")

    private var mongo: Option[MongoServer] = None

    override def start {
      mongo = EmbeddedMongo.start
      try {
        FixtureImporter.importFixtures(mongoTemplate)
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
  }

  trait ExternalProps {
    def configFiles = List(
      "../module-install-parent/config/common/omatsivut/omatsivut.properties",
      "./module-install-parent/config/common/omatsivut/omatsivut.properties",
      System.getProperty("user.home") + "/oph-configuration/common.properties", // for server environments
      System.getProperty("user.home") + "/oph-configuration/omatsivut.properties"
    )
  }

  trait StubbedExternalDeps extends TestMode {
    def authenticationInfoService: AuthenticationInfoService = new AuthenticationInfoService {
      def getHenkiloOID(hetu: String) = hetu match {
        case "010101-123N" => Some("1.2.246.562.24.14229104472")
        case _ => None
      }
    }
  }

  trait TestMode extends AppConfig {
    override def isTest = true
  }

  trait AppConfig {
    def authenticationInfoService: AuthenticationInfoService
    def springConfiguration: OmatSivutConfiguration
    lazy val springContext = new OmatSivutSpringContext(OmatSivutSpringContext.createApplicationContext(this))

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
  }
}

case class RemoteApplicationConfig(url: String, username: String, password: String, path: String, ticketConsumerPath: String)