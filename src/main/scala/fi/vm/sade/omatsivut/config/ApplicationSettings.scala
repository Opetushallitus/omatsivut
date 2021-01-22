package fi.vm.sade.omatsivut.config

import com.typesafe.config.Config
import fi.vm.sade.groupemailer.GroupEmailerSettings
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.db.impl.{DbConfig, OmatsivutDb}
import fi.vm.sade.utils.captcha.CaptchaServiceSettings

case class ApplicationSettings(config: Config) extends GroupEmailerSettings(config) {

  val captchaSettings = new CaptchaServiceSettings(config)
  val securitySettings = new SecuritySettings(config)
  val s3Settings = new S3Settings(config)

  val authenticationServiceConfig : RemoteApplicationConfig = getRemoteApplicationConfig(config.getConfig("omatsivut.authentication-service"))
  val tuloskirjeetFileSystemUrl : String = config.getString("omatsivut.tuloskirjeet.filesystem.url")

  val omatsivutDbConfig = DbConfig(
    url = config.getString("omatsivut.db.url"),
    user = getString(config, "omatsivut.db.user"),
    password = getString(config, "omatsivut.db.password"),
    maxConnections = getInt(config, "omatsivut.db.maxConnections"),
    minConnections = getInt(config, "omatsivut.db.minConnections"),
    numThreads = getInt(config, "omatsivut.db.numThreads"),
    queueSize = getInt(config, "omatsivut.db.queueSize"),
    registerMbeans = getBoolean(config, "omatsivut.db.registerMbeans"),
    initializationFailTimeout = getLong(config, "omatsivut.db.initializationFailFast"),
    leakDetectionThresholdMillis = getLong(config, "omatsivut.db.leakDetectionThresholdMillis")
  )

  val sessionTimeoutSeconds = getInt(config, "omatsivut.sessionTimeoutSeconds")
  val sessionCleanupCronString = getString(config, "omatsivut.sessionCleanupCronString")

  val aesKey : String = config.getString("omatsivut.crypto.aes.key")
  val hmacKey : String = config.getString("omatsivut.crypto.hmac.key")

  val oppijaBaseUrlEn = config.getString("oppija.base.url.en")
  val oppijaBaseUrlFi = config.getString("oppija.base.url.fi")
  val oppijaBaseUrlSv = config.getString("oppija.base.url.sv")

  val kohdejoukotKorkeakoulu: List[String] = config.getString("omatsivut.kohdejoukot.korkeakoulu").split(",").toList
  val kohdejoukotToinenAste: List[String] = config.getString("omatsivut.kohdejoukot.toinen-aste").split(",").toList
  val kohdejoukonTarkenteetSiirtohaku: List[String] = config.getString("omatsivut.kohdejoukon-tarkenteet.siirtohaku").split(",").toList

  private def getRemoteApplicationConfig(config: Config) = {
    RemoteApplicationConfig(
      OphUrlProperties.url("url-oppijanumerorekisteri-service"),
      config.getString("username"),
      config.getString("password"),
      config.getString("ticket_consumer_path"),
      config
    )
  }

  private def getString(config: Config, key: String): Option[String] = {
    if (config.hasPath(key)) Some(config.getString(key)) else None
  }

  private def getInt(config: Config, key: String): Option[Int] = {
    if (config.hasPath(key)) Some(config.getInt(key)) else None
  }

  private def getLong(config: Config, key: String): Option[Long] = {
    if (config.hasPath(key)) Some(config.getLong(key)) else None
  }

  private def getBoolean(config: Config, key: String): Option[Boolean] = {
    if (config.hasPath(key)) Some(config.getBoolean(key)) else None
  }

}

object ApplicationSettingsParser extends fi.vm.sade.utils.config.ApplicationSettingsParser[ApplicationSettings] {
  override def parse(config: Config) = ApplicationSettings(config)
}

class SecuritySettings(c: Config) {
  val casUrl : String = OphUrlProperties.url("cas.url")
  val casUsername : String = c.getString("cas.username")
  val casPassword : String = c.getString("cas.password")
  val casVirkailijaUrl : String = OphUrlProperties.url("cas.virkailija.url")
  val casVirkailijaUsername : String = c.getString("cas.virkailija.username")
  val casVirkailijaPassword : String = c.getString("cas.virkailija.password")
  val casOppijaUrl : String = OphUrlProperties.url("cas.oppija.url")
  val casOppijaUsername : String = c.getString("cas.oppija.username")
  val casOppijaPassword : String = c.getString("cas.oppija.password")
}

class S3Settings(c: Config) {
  val region : String = c.getString("omatsivut.s3_region")
  val bucket : String = c.getString("omatsivut.s3_bucket")
}
