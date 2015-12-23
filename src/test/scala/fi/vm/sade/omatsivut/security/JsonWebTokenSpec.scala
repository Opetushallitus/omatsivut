package fi.vm.sade.omatsivut.security

import org.junit.runner.RunWith
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class JsonWebTokenSpec extends MutableScalatraSpec with Mockito {

  val jwt = new JsonWebToken("secret")
  val hakemusOid = "1.2.3"

  "JsonWebToken" should {

    "return claim on valid token" in {
      val claim = Map("hakemusOid" -> hakemusOid)
      val token = jwt.encode(claim)

      jwt.decode(token) must beSuccessfulTry.withValue(claim)
    }

    "return failure on invalid token signature" in {
      val claim = Map("hakemusOid" -> hakemusOid)
      val token = jwt.encode(claim).replace("IrTpS2", "ArTpS2")

      jwt.decode(token) must beFailedTry.withThrowable[InvalidJsonWebTokenException]
    }

    "throw runtime exception if initialized without a secret" in {
      new JsonWebToken("") must throwA[RuntimeException]
    }

  }

}
