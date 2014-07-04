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
    def authenticationInfoService: AuthenticationInfoService = new RemoteAuthenticationInfoService(settings.authenticationService)
  }

  object Dev extends StubbedConfig {}
  object IT extends StubbedConfig {}

  trait StubbedConfig extends AppConfig {
    def authenticationInfoService: AuthenticationInfoService = new AuthenticationInfoService {
      def getHenkiloOID(hetu: String) = hetu match {
        case "010101-123N" => Some("1.2.246.562.24.14229104472")
        case _ => None
      }
    }
  }

  trait AppConfig {
    val settings: ApplicationSettings = ApplicationSettings.loadSettings
    def authenticationInfoService: AuthenticationInfoService
  }

  def settings = config.settings
}

case class RemoteApplicationConfig(url: String, username: String, password: String, path: String, ticketConsumerPath: String)