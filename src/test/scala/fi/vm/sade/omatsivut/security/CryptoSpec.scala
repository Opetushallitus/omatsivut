package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig
import org.specs2.mutable.Specification

class CryptoSpec extends Specification {
  "AES crypto" should {
    "encrypt and decrypt" in {
      val cipher: AuthenticationCipher = AuthenticationCipher()(AppConfig.fromSystemProperty)
      val encrypted = cipher.encrypt("testString")
      cipher.decrypt(encrypted) must_== "testString"
    }
  }
}
