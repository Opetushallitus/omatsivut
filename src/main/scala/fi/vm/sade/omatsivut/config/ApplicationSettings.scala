package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.groupemailer.GroupEmailerSettings
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.captcha.CaptchaServiceSettings

case class ApplicationSettings(config: Config) extends GroupEmailerSettings(config) {

  val captchaSettings = new CaptchaServiceSettings(config)
  val securitySettings = new SecuritySettings(config)
  val s3Settings = new S3Settings(config)

  val authenticationServiceConfig : RemoteApplicationConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))
  val tuloskirjeetFileSystemUrl : String = config.getString("omatsivut.tuloskirjeet.filesystem.url")

  val aesKey : String = config.getString("omatsivut.crypto.aes.key")
  val hmacKey : String = config.getString("omatsivut.crypto.hmac.key")

  val oppijaBaseUrlEn = config.getString("oppija.base.url.en")
  val oppijaBaseUrlFi = config.getString("oppija.base.url.fi")
  val oppijaBaseUrlSv = config.getString("oppija.base.url.sv")

  private def getRemoteApplicationConfig(config: Config) = {
    RemoteApplicationConfig(
      OphUrlProperties.url("url-oppijanumerorekisteri-service"),
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
  val casUrl : String = OphUrlProperties.url("cas.url")
  val casUsername : String = c.getString("cas.username")
  val casPassword : String = c.getString("cas.password")
}

class S3Settings(c: Config) {
  val region : String = c.getString("omatsivut.s3_region")
  val bucket : String = c.getString("omatsivut.s3_bucket")
}
