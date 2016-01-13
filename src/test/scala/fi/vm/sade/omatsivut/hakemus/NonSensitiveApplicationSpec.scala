package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.HakutoiveData
import fi.vm.sade.hakemuseditori.hakemus.domain.{Hakemus, HakemusMuutos}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.omatsivut._
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import fi.vm.sade.omatsivut.servlet.{InsecureHakemus, InsecureHakemusInfo}
import org.json4s.jackson.Serialization
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class NonSensitiveApplicationSpec extends ScalatraTestSupport {
  implicit val jsonFormats = JsonFormats.jsonFormats ++ List(new HakemuksenTilaSerializer, new NonSensitiveHakemusSerializer, new NonSensitiveHakemusInfoSerializer)
  private val jwt = new JsonWebToken("akuankkaakuankka")
  val hakemusOid = "1.2.246.562.11.00000000178"
  val personOid = "1.2.246.562.24.14229104472"
  val hakutoiveData: List[HakutoiveData] = List(
    Map(
      "Opetuspiste-id" -> "1.2.246.562.10.78522729439",
      "Opetuspiste" -> "Taideyliopisto, Sibelius-Akatemia",
      "Koulutus" -> "Jazzmusiikki, sävellys 2,5-vuotinen koulutus",
      "Koulutus-id-attachmentgroups" -> "",
      "Koulutus-id-ao-groups" -> "1.2.246.562.28.11386525208,1.2.246.562.28.26079071193,1.2.246.562.28.32672180497,1.2.246.562.28.88952350290,1.2.246.562.28.25992753605",
      "Koulutus-id-kaksoistutkinto" -> "false",
      "Koulutus-id-sora" -> "false",
      "Koulutus-id-vocational" -> "false",
      "Koulutus-id-attachments" -> "true",
      "Koulutus-id-lang" -> "SV",
      "Koulutus-id-aoIdentifier" -> "",
      "Koulutus-id-athlete" -> "false",
      "Koulutus-educationDegree" -> "koulutusasteoph2002_72",
      "yoLiite" -> "true",
      "Koulutus-id" -> "1.2.246.562.20.14660127086",
      "Koulutus-id-educationcode" -> "koulutus_723111"),
    Map(
      "Opetuspiste-id" -> "1.2.246.562.10.78522729439",
      "Opetuspiste" -> "Taideyliopisto,  Sibelius-Akatemia",
      "Koulutus" -> "Jazzmusiikki, sävellys 5,5-vuotinen koulutus",
      "Koulutus-id-ao-groups" -> "1.2.246.562.28.11386525208,1.2.246.562.28.26079071193,1.2.246.562.28.25992753605,1.2.246.562.28.32672180497",
      "Koulutus-id-kaksoistutkinto" -> "false",
      "Koulutus-id-sora" -> "false",
      "Koulutus-id-vocational" -> "false",
      "Koulutus-id-attachments" -> "true",
      "Koulutus-id-lang" -> "SV",
      "Koulutus-id-aoIdentifier" -> "",
      "Koulutus-id-athlete" -> "false",
      "Koulutus-educationDegree" -> "koulutusasteoph2002_72",
      "yoLiite" -> "true",
      "Koulutus-id" -> "1.2.246.562.20.18094409226",
      "Koulutus-id-educationcode" -> "koulutus_723111"))

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
      val hakemusJWT = HakemusJWT(hakemusOid, answersInJWT, personOid)
      get("insecure/applications/application/session", headers = Map("Authorization" -> s"Bearer ${jwt.encode(hakemusJWT)}")) {
        val hakemusInfo = Serialization.read[InsecureHakemusInfo](body).response.hakemusInfo
        NonSensitiveHakemusInfo.answerIds(hakemusInfo.hakemus.answers) must beEqualTo(
          NonSensitiveHakemusInfo.nonSensitiveAnswers ++ answersInJWT)
        hakemusInfo.questions.flatMap(_.flatten.flatMap(_.answerIds)).toSet must beEqualTo(answersInJWT)
      }
    }

    "does not have answers that have not been added in this session" in {
      "in validate result" in {
        val answersInJWT: Set[AnswerId] = Set(AnswerId("hakutoiveet", "54773037e4b0c2bb60201414"))
        val answersInPost = Hakemus.emptyAnswers ++ Map(
          "hakutoiveet" -> Map(
            "54773037e4b0c2bb60201414" -> "ope",
            "54774050e4b0c2bb60201431" -> "dummy"))
        val hakemusJWT = HakemusJWT(hakemusOid, answersInJWT, personOid)
        post("insecure/applications/validate/" + hakemusOid,
          body = Serialization.write(HakemusMuutos(hakemusOid, "1.2.246.562.29.95390561488", hakutoiveData, answersInPost)),
          headers = Map("Authorization" -> s"Bearer ${jwt.encode(hakemusJWT)}")) {
          val hakemusInfo = Serialization.read[InsecureHakemusInfo](body).response.hakemusInfo
          NonSensitiveHakemusInfo.answerIds(hakemusInfo.hakemus.answers) must beEqualTo(
            NonSensitiveHakemusInfo.nonSensitiveAnswers ++ answersInJWT)
          hakemusInfo.questions.flatMap(_.flatten.flatMap(_.answerIds)).toSet must beEqualTo(answersInJWT)
        }
      }
      "in PUT result" in {
        val answersInJWT: Set[AnswerId] = Set(AnswerId("hakutoiveet", "54773037e4b0c2bb60201414"))
        val answersInPost = Hakemus.emptyAnswers ++ Map(
          "hakutoiveet" -> Map(
            "54773037e4b0c2bb60201414" -> "ope",
            "54774050e4b0c2bb60201431" -> "option_1"))
        val hakemusJWT = HakemusJWT(hakemusOid, answersInJWT, personOid)
        put("insecure/applications/" + hakemusOid,
          body = Serialization.write(HakemusMuutos(hakemusOid, "1.2.246.562.29.95390561488", hakutoiveData, answersInPost)),
          headers = Map("Authorization" -> s"Bearer ${jwt.encode(hakemusJWT)}")) {
          val hakemus = Serialization.read[InsecureHakemus](body).response.hakemus
          NonSensitiveHakemusInfo.answerIds(hakemus.answers) must beEqualTo(
            NonSensitiveHakemusInfo.nonSensitiveAnswers ++ answersInJWT)
        }
      }
    }
  }

}
