package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import fi.vm.sade.omatsivut.servlet.InsecureHakemusInfo
import fi.vm.sade.omatsivut.{NonSensitiveHakemusInfo, NonSensitiveHakemusInfoSerializer, NonSensitiveHakemusSerializer, TimeWarp}
import org.json4s.Formats
import org.json4s.jackson.Serialization
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NonSensitiveApplicationSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {
  override implicit val jsonFormats: Formats = JsonFormats.jsonFormats ++ List(new HakemuksenTilaSerializer, new NonSensitiveHakemusSerializer, new NonSensitiveHakemusInfoSerializer)
  private val jwt = new JsonWebToken("akuankkaakuankka")

  "NonSensitiveApplication" should {
    "has only nonsensitive contact info when fetched with a token" in {
      get("insecure/applications/application/token/dummytoken") {
        val hakemusInfo = Serialization.read[InsecureHakemusInfo](body).response.hakemusInfo
        NonSensitiveHakemusInfo.answerIds(hakemusInfo.hakemus.answers) must beEqualTo(NonSensitiveHakemusInfo.nonSensitiveAnswers)
        hakemusInfo.questions must beEmpty
      }
    }

    "has nonsensitive contact info and answers stored in JWT when fetched with a JWT" in {
      val answersInJWT: Set[AnswerId] = Set(AnswerId("hakutoiveet", "54773037e4b0c2bb60201414"))
      val hakemusJWT = HakemusJWT("1.2.246.562.11.00000000178", answersInJWT, "personOid")
      get("insecure/applications/application/session", headers = Map("Authorization" -> s"Bearer ${jwt.encode(hakemusJWT)}")) {
        val hakemusInfo = Serialization.read[InsecureHakemusInfo](body).response.hakemusInfo
        NonSensitiveHakemusInfo.answerIds(hakemusInfo.hakemus.answers) must beEqualTo(
          NonSensitiveHakemusInfo.nonSensitiveAnswers ++ answersInJWT)
        hakemusInfo.questions.flatMap(_.flatten.flatMap(_.answerIds)).toSet must beEqualTo(answersInJWT)
      }
    }
  }

}
