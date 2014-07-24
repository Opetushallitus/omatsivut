package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class UpdateApplicationSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT
  sequential

  addServlet(new ApplicationsServlet(), "/*")

  "PUT /application/:oid" should {
    "reject application with empty hakutoiveet" in {
      modifyHakemus { hakemus => hakemus.copy(hakutoiveet = Nil) } { _ =>
        status must_== 400
      }
    }

    "accept valid application" in {
      modifyHakemus { hakemus => hakemus} { hakemus =>
        val result: JValue = JsonMethods.parse(body)
        status must_== 200
        compareWithoutTimestamp(hakemus, result.extract[Hakemus]) must_== true
      }
    }

    "save application" in {
      modifyHakemus(answerExtraQuestion(preferencesPhaseKey, "539158b8e4b0b56e67d2c74b", "yes sir")) { newHakemus =>
        status must_== 200
        val result: JValue = JsonMethods.parse(body)
        compareWithoutTimestamp(newHakemus, result.extract[Hakemus]) must_== true
        // verify saved application
        withSavedApplication(newHakemus) { application =>
          application.getPhaseAnswers(personalInfoPhaseKey).get(ssnKey) must_== testHetu
          application.getPhaseAnswers(preferencesPhaseKey).get("539158b8e4b0b56e67d2c74b") must_== "yes sir"
        }
      }
    }

    "prune answers to removed questions" in {
      modifyHakemus(answerExtraQuestion(preferencesPhaseKey, "539158b8e4b0b56e67d2c74b", "yes sir")) { _ =>
        status must_== 200
        modifyHakemus(removeHakutoive) { hakemus =>
          status must_== 200
          withSavedApplication(hakemus) { application =>
            application.getPhaseAnswers(preferencesPhaseKey).containsKey("539158b8e4b0b56e67d2c74b") must_== false
          }
        }
      }
    }

    "reject answers to unknown questions" in {
      modifyHakemus(answerExtraQuestion(preferencesPhaseKey, "unknown", "hacking")) { hakemus =>
        status must_== 400
      }
    }
  }
}