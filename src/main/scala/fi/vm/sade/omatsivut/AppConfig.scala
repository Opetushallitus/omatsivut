package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.security.{RemoteAuthenticationInfoService, AuthenticationInfoService}

object AppConfig extends Logging {
  lazy val config: AppConfig = {
    System.getProperty("omatsivut.profile", "default") match {
      case "default" => Default
      case "dev" => Dev
      case "it" => IT
      case name => throw new IllegalArgumentException("Unknown value for omatsivut.profile: " + name);
    }
  }

  object Default extends AppConfig {
    val settings = ApplicationSettings.loadSettings(List(
      "../module-install-parent/config/common/omatsivut/omatsivut.properties",
      "./module-install-parent/config/common/omatsivut/omatsivut.properties",
      System.getProperty("user.home") + "/oph-configuration/common.properties", // for server environments
      System.getProperty("user.home") + "/oph-configuration/omatsivut.properties"
    ))
    def authenticationInfoService: AuthenticationInfoService = new RemoteAuthenticationInfoService(settings.authenticationService)
  }

  object Dev extends StubbedConfig {}
  object IT extends StubbedConfig {}

  trait StubbedConfig extends AppConfig {
    val settings = ApplicationSettings.loadSettings(List("src/main/resources/reference.conf"))
    def authenticationInfoService: AuthenticationInfoService = new AuthenticationInfoService {
      def getHenkiloOID(hetu: String) = hetu match {
        case "010101-123N" => Some("1.2.246.562.24.14229104472")
        case _ => None
      }
    }
    override def usesFixtures = true
    override def springProfile = "dev"
    System.getProperty("omatsivut.configFile")
  }

  trait AppConfig {
    def settings: ApplicationSettings
    def authenticationInfoService: AuthenticationInfoService
    def usesFixtures = false
    def springProfile = "default"
  }

  // Maybe this global should be removed
  def settings = config.settings
}

case class RemoteApplicationConfig(url: String, username: String, password: String, path: String, ticketConsumerPath: String)