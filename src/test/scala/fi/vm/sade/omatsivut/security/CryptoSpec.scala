package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class CryptoSpec extends Specification {
  "AES crypto" should {
    "encrypt and decrypt" in {
      val appConfig: AppConfig = AppConfig.fromSystemProperty
      val cipher: AuthenticationCipher = new AuthenticationCipher(appConfig.settings.aesKey, appConfig.settings.hmacKey)
      val encrypted = cipher.encrypt("testString")
      cipher.decrypt(encrypted) must_== "testString"
    }
  }
}
