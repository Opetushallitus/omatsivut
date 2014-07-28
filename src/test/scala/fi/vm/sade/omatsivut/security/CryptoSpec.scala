package fi.vm.sade.omatsivut.security

import org.specs2.mutable.Specification
import fi.vm.sade.omatsivut.AppConfig

class CryptoSpec extends Specification {
  "AES crypto" should {
    "encrypt and decrypt" in {
      val cipher: AuthenticationCipher = AuthenticationCipher()(AppConfig.fromSystemProperty)
      val encrypted = cipher.encrypt("testString")
      cipher.decrypt(encrypted) must_== "testString"
    }
  }
}
