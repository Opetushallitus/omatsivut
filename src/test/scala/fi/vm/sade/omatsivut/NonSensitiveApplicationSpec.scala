package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.hakemus.{FixturePerson, HakemuksenTilaSerializer, HakemusApiSpecification}
import fi.vm.sade.omatsivut.servlet.InsecureHakemusInfo
import org.json4s.Formats
import org.json4s.jackson.Serialization
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NonSensitiveApplicationSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {
  override implicit val jsonFormats: Formats = JsonFormats.jsonFormats ++ List(new HakemuksenTilaSerializer, new NonSensitiveHakemusSerializer, new NonSensitiveHakemusInfoSerializer)

  "NonSensitiveApplication" should {
    "has only nonsensitive contact info when fetched with a token" in {
      get("insecure/applications/application/token/dummytoken") {
        val hakemusInfo = Serialization.read[InsecureHakemusInfo](body).response.hakemusInfo
        NonSensitiveHakemusInfo.answerIds(hakemusInfo.hakemus.answers) must beEqualTo(NonSensitiveHakemusInfo.nonSensitiveAnswers)
        hakemusInfo.questions must beEmpty
      }
    }
  }

}
