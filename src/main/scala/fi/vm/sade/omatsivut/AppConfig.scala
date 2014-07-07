package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.security.{RemoteAuthenticationInfoService, AuthenticationInfoService}

object AppConfig extends Logging {
  lazy val config: AppConfig = {
    val profile: String = System.getProperty("omatsivut.profile", "default")
    logger.info("Using omatsivut.profile=" + profile)
    profile match {
      case "default" => Default
      case "dev" => Dev
      case "dev-remote-mongo" => DevWithRemoteMongo
      case "it" => IT
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  object Default extends AppConfig with ExternalProps {
    def authenticationInfoService: AuthenticationInfoService = new RemoteAuthenticationInfoService(settings.authenticationService)
    def springConfiguration = OmatSivutSpringContext.Default
  }

  object Dev extends AppConfig with StubbedExternalDeps with ReferenceProps with Fixtures {
    def springConfiguration = OmatSivutSpringContext.Dev
  }
  object DevWithRemoteMongo extends StubbedExternalDeps with ExternalProps {
    def springConfiguration = OmatSivutSpringContext.DevWithRemoteMongo
  }
  
  object IT extends StubbedExternalDeps with ReferenceProps with Fixtures {
    def springConfiguration = OmatSivutSpringContext.IT
  }

  trait ReferenceProps {
    val settings = ApplicationSettings.loadSettings(List("src/main/resources/reference.conf"))
  }

  trait ExternalProps {
    val settings = ApplicationSettings.loadSettings(List(
      "../module-install-parent/config/common/omatsivut/omatsivut.properties",
      "./module-install-parent/config/common/omatsivut/omatsivut.properties",
      System.getProperty("user.home") + "/oph-configuration/common.properties", // for server environments
      System.getProperty("user.home") + "/oph-configuration/omatsivut.properties"
    ))
    val usesFixtures = false
  }

  trait Fixtures extends TestMode {
    val usesFixtures = true
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
    def settings: ApplicationSettings
    def authenticationInfoService: AuthenticationInfoService
    def usesFixtures: Boolean
    def springConfiguration: OmatSivutConfiguration
    def isTest: Boolean = false
  }

  // Maybe this global should be removed
  def settings = config.settings
}

case class RemoteApplicationConfig(url: String, username: String, password: String, path: String, ticketConsumerPath: String)