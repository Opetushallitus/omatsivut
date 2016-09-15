package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.groupemailer.GroupEmailerSettings
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.captcha.CaptchaServiceSettings

case class ApplicationSettings(config: Config) extends GroupEmailerSettings(config) {
  OphUrlProperties.addOverride("host.oppija", config.getString("host.oppija"))
  OphUrlProperties.addOverride("host.virkailija", config.getString("host.virkailija"))

  val captchaSettings = new CaptchaServiceSettings(config)

  val raamitUrl = OphUrlProperties.url("oppija-raamit.base")

  val piwikUrl = OphUrlProperties.url("piwik.base")

  val securitySettings = new SecuritySettings(config)
  val authenticationServiceConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val valintaTulosServiceUrl = OphUrlProperties.url("valinta-tulos-service.base")
  val koulutusinformaatioAoUrl = OphUrlProperties.url("koulutusinformaatio.ao")
  val koulutusinformaatioLopUrl = OphUrlProperties.url("koulutusinformaatio.lop")
  val koulutusinformaationBIUrl = OphUrlProperties.url("koulutusinformaatio.basketitems")

  val muistilistaUrl = OphUrlProperties.url("koulutusinformaatio.muistilista")

  val ohjausparametritUrl = OphUrlProperties.url("ohjausparametrit.kaikki")
  val tarjontaUrl = OphUrlProperties.url("tarjonta-service.base")
  val viestintapalveluUrl = OphUrlProperties.url("viestintapalvelu.base")
  val koodistoUrl = OphUrlProperties.url("koodisto-service.base")
  val tuloskirjeetFileSystemUrl = config.getString("omatsivut.tuloskirjeet.filesystem.url")

  val aesKey = config.getString("omatsivut.crypto.aes.key")
  val hmacKey = config.getString("omatsivut.crypto.hmac.key")

  val oppijanTunnistusVerifyUrl = OphUrlProperties.url("oppijan-tunnistus.verify")

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
  val casUrl = OphUrlProperties.url("cas.base")
  val casUsername = c.getString("cas.username")
  val casPassword = c.getString("cas.password")
}
