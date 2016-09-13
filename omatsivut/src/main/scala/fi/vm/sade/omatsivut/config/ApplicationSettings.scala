package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.groupemailer.GroupEmailerSettings
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.captcha.CaptchaServiceSettings

case class ApplicationSettings(config: Config) extends GroupEmailerSettings(config) {
  
  val ophUrlProperties =  AppConfig.ophUrlProperties
  
  val captchaSettings = new CaptchaServiceSettings(config)

  val raamitUrl = ophUrlProperties.url("oppija-raamit.base")

  val piwikUrl = ophUrlProperties.url("piwik.base")

  val securitySettings = new SecuritySettings(config, ophUrlProperties)
  val authenticationServiceConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val valintaTulosServiceUrl = ophUrlProperties.url("valinta-tulos-service.base")
  val koulutusinformaatioAoUrl = ophUrlProperties.url("koulutusinformaatio.ao")
  val koulutusinformaatioLopUrl = ophUrlProperties.url("koulutusinformaatio.lop")
  val koulutusinformaationBIUrl = ophUrlProperties.url("koulutusinformaatio.basketitems")

  val muistilistaUrl = ophUrlProperties.url("koulutusinformaatio.muistilista")

  val ohjausparametritUrl = ophUrlProperties.url("ohjausparametrit.kaikki")
  val tarjontaUrl = ophUrlProperties.url("tarjonta-service.base")
  val viestintapalveluUrl = ophUrlProperties.url("viestintapalvelu.base")
  val koodistoUrl = ophUrlProperties.url("koodisto-service.base")
  val tuloskirjeetFileSystemUrl = config.getString("omatsivut.tuloskirjeet.filesystem.url")

  val aesKey = config.getString("omatsivut.crypto.aes.key")
  val hmacKey = config.getString("omatsivut.crypto.hmac.key")

  val oppijanTunnistusVerifyUrl = ophUrlProperties.url("oppijan-tunnistus.verify")

  private def getRemoteApplicationConfig(config: Config) = {
    RemoteApplicationConfig(
      ophUrlProperties.url("authentication-service.base"),
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

class SecuritySettings(c: Config, ophUrlProperties: OphUrlProperties) {
  val casUrl = ophUrlProperties.url("cas.base")
  val casUsername = c.getString("cas.username")
  val casPassword = c.getString("cas.password")
}
