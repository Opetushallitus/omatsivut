package fi.vm.sade.omatsivut

import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
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
    def springConfiguration = new OmatSivutSpringContext.Default()
  }

  class Dev extends AppConfig with MockAuthentication {
    def springConfiguration = new OmatSivutSpringContext.Dev()
    def configFiles = List("src/main/resources/dev.conf")
  }
  class DevWithRemoteMongo extends MockAuthentication with ExternalProps {
    def springConfiguration = new OmatSivutSpringContext.Dev()
  }

  class IT extends MockAuthentication with StubbedExternalDeps {
    def springConfiguration = new OmatSivutSpringContext.IT()
    def configFiles = List("src/main/resources/it.conf")

    private var mongo: Option[MongoServer] = None

    override def start {
      mongo = EmbeddedMongo.start
      try {
        MongoFixtureImporter.importJsonFixtures(mongoTemplate)
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
  }

  trait MockAuthentication extends TestMode {
  }

  trait TestMode extends AppConfig {
    override def isTest = true
  }

  trait AppConfig {
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