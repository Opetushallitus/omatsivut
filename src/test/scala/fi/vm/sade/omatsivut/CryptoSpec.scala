package fi.vm.sade.omatsivut

import org.specs2.mutable.Specification
import fi.vm.sade.omatsivut.security.AuthenticationCipher

class CryptoSpec extends Specification {

  "AES crypto" should {
    "encrypt and decrypt" in {
      val encrypted = AuthenticationCipher.encrypt("testString")
      AuthenticationCipher.decrypt(encrypted) must_== "testString"
    }
  }
}
