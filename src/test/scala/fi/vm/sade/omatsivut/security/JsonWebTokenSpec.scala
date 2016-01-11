package fi.vm.sade.omatsivut.security

import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import org.json4s.JsonDSL.WithBigDecimal._
import org.junit.runner.RunWith
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import org.specs2.runner.JUnitRunner
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

@RunWith(classOf[JUnitRunner])
class JsonWebTokenSpec extends MutableScalatraSpec with Mockito {

  val jwt = new JsonWebToken("secret")
  val hakemusOid = "1.2.3"
  val answersFromThisSession = Set(AnswerId("foo", "bar"), AnswerId("bar", "foo"))
  val personOid = "9.7.3"

  "JsonWebToken" should {

    "return claim on valid token" in {
      val token = jwt.encode(HakemusJWT(hakemusOid, answersFromThisSession, personOid))
      jwt.decode(token) must beSuccessfulTry.withValue(HakemusJWT(hakemusOid, answersFromThisSession, personOid))
    }

    "return failure on invalid token signature" in {
      val expectedSignature = ".SRqRG2MnOPddgwf_obkUUpmYi-fqB2tEZcd4bPaQPds"
      val token = jwt.encode(HakemusJWT(hakemusOid, answersFromThisSession, personOid)).replace(expectedSignature, ".ArTpS2")
      jwt.decode(token) must beFailedTry.withThrowable[InvalidJsonWebTokenException]
    }

    "throw exception on invalid claim" in {
      val token = JwtJson4s.encode(Map("lorem" -> "larem"), "secret", JwtAlgorithm.HS256)
      jwt.decode(token) must beFailedTry.withThrowable[InvalidJsonWebTokenException]
    }

    "throw runtime exception if initialized without a secret" in {
      new JsonWebToken("") must throwA[RuntimeException]
    }

  }

}
