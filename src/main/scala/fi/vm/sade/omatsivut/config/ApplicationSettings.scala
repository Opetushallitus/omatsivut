package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.groupemailer.GroupEmailerSettings
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.captcha.CaptchaServiceSettings

case class ApplicationSettings(config: Config) extends GroupEmailerSettings(config) {

  val captchaSettings = new CaptchaServiceSettings(config)

  val securitySettings = new SecuritySettings(config)
  val authenticationServiceConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val tuloskirjeetFileSystemUrl = config.getString("omatsivut.tuloskirjeet.filesystem.url")

  val aesKey = config.getString("omatsivut.crypto.aes.key")
  val hmacKey = config.getString("omatsivut.crypto.hmac.key")

  private def getRemoteApplicationConfig(config: Config) = {
    RemoteApplicationConfig(
      OphUrlProperties.url("authentication-service.base"),
      config.getString("username"),
      config.getString("password"),
      config.getString("ticket_consumer_path"),
      config
    )
  }
}

object ApplicationSettingsParser extends fi.vm.sade.utils.config.ApplicationSettingsParser[ApplicationSettings] {
  override def parse(config: Config) = ApplicationSettings(config)
}

class SecuritySettings(c: Config) {
  val casUrl = OphUrlProperties.url("cas.url")
  val casUsername = c.getString("cas.username")
  val casPassword = c.getString("cas.password")
}
