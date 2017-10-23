package fi.vm.sade.omatsivut.security

import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import org.json4s.JsonDSL.WithBigDecimal._
import org.json4s.NoTypeHints
import org.json4s.jackson.Serialization.formats
import org.junit.runner.RunWith
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner
import pdi.jwt.{JwtAlgorithm, JwtJson4s}

@RunWith(classOf[JUnitRunner])
class JsonWebTokenSpec extends Specification with Mockito {
  val jwt = new JsonWebToken("akuankkaakuankkaakuankkaakuankka")
  implicit val jsonFormats = formats(NoTypeHints)
  val hakemusOid = "1.2.3"
  val answersFromThisSession = Set(AnswerId("foo", "bar"), AnswerId("bar", "foo"))
  val personOid = "9.7.3"

  "JsonWebToken" should {

    "return claim on valid token" in {
      val token = jwt.encode(HakemusJWT(hakemusOid, answersFromThisSession, personOid))
      jwt.decode(token) must beSuccessfulTry.withValue(HakemusJWT(hakemusOid, answersFromThisSession, personOid))
    }

    "return failure on invalid token signature" in {
      val token = "foobar"
      jwt.decode(token) must beFailedTry.withThrowable[RuntimeException]
    }

    "throw exception on invalid claim" in {
      val token = JwtJson4s.encode(Map("lorem" -> "larem"), "secret", JwtAlgorithm.HS256)
      jwt.decode(token) must beFailedTry.withThrowable[RuntimeException]
    }

    "throw runtime exception if initialized without a secret" in {
      new JsonWebToken("") must throwA[RuntimeException]
    }

    "create new oili jwt" in {
      val token = jwt.createOiliJwt(hakemusOid)
      val decoded = JwtJson4s.decodeJson(token, "akuankkaakuankkaakuankkaakuankka", Seq(JwtAlgorithm.HS256)).get.extract[OiliJWT]
      decoded.hakijaOid mustEqual hakemusOid
      decoded.expires must be_>(System.currentTimeMillis)
      decoded.expires - System.currentTimeMillis must be_<=(3600L * 2 * 1000)
    }
  }
}
