package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.groupemailer.GroupEmailerSettings
import fi.vm.sade.utils.cas.CasConfig

case class ApplicationSettings(config: Config) extends GroupEmailerSettings(config) {
  val casTicketUrl = config.getString("omatsivut.cas.ticket.url")

  val raamitUrl = config.getString("omatsivut.oppija-raamit.url")

  val piwikUrl = config.getString("omatsivut.piwik.url")

  val securitySettings = new SecuritySettings(config)
  val authenticationServiceConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))

  val valintaTulosServiceUrl = config.getString("omatsivut.valinta-tulos-service.url")
  val koulutusinformaatioAoUrl = config.getString("omatsivut.koulutusinformaatio.ao.url")
  val koulutusinformaatioLopUrl = config.getString("omatsivut.koulutusinformaatio.lop.url")
  val koulutusinformaationBIUrl = config.getString("omatsivut.koulutusinformaatio.basketitems.url")

  val muistilistaUrl = config.getString("muistilista.url")

  val recaptchaUrl = config.getString("recaptcha.url")
  val recaptchaSecret = config.getString("recaptcha.secret")

  val ohjausparametritUrl = config.getString("omatsivut.ohjausparametrit.url")
  val tarjontaUrl = config.getString("omatsivut.tarjonta.url")
  val koodistoUrl = config.getString("cas.service.koodisto-service")

  val aesKey = config.getString("omatsivut.crypto.aes.key")
  val hmacKey = config.getString("omatsivut.crypto.hmac.key")

  private def getRemoteApplicationConfig(config: Config) = {
    RemoteApplicationConfig(
      config.getString("url"),
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
  val casConfig = CasConfig(c.getString("cas.url"))
  val casUsername = c.getString("cas.username")
  val casPassword = c.getString("cas.password")
}