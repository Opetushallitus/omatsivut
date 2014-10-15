package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.PersonOid
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus
import org.json4s._
import org.json4s.jackson.JsonMethods

class UpdateApplicationSpec extends HakemusApiSpecification with FixturePerson {
  override implicit lazy val appConfig = new AppConfig.IT
  sequential

  addServlet(componentRegistry.newApplicationsServlet, "/api/applications")

  "PUT /application/:oid" should {
    "reject application with empty hakutoiveet" in {
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemus => hakemus.copy(hakutoiveet = Nil) } { _ =>
        status must_== 400
      }
    }

    "reject application with different person oid" in {
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemus =>
        saveHakemus(hakemus) {
          status must_== 403
        } (PersonOid("wat"))
      }
    }

    "accept valid application" in {
      modifyHakemus (hakemusNivelKesa2013WithPeruskouluBaseEducationId){ hakemus => hakemus} { hakemus =>
        val result: JValue = JsonMethods.parse(body)
        status must_== 200
        hasSameHakuToiveet(hakemus, result.extract[Hakemus]) must_== true
      }
    }

    "save application" in {
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(answerExtraQuestion(preferencesPhaseKey, "539158b8e4b0b56e67d2c74b", "yes sir")) { newHakemus =>
        status must_== 200
        val result: JValue = JsonMethods.parse(body)
        hasSameHakuToiveet(newHakemus, result.extract[Hakemus]) must_== true
        // verify saved application
        withSavedApplication(newHakemus) { application =>
          application.getPhaseAnswers(personalInfoPhaseKey).get(ssnKey) must_== testHetu
          application.getPhaseAnswers(preferencesPhaseKey).get("539158b8e4b0b56e67d2c74b") must_== "yes sir"
        }
      }
    }

    "prune answers to removed questions" in {
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(answerExtraQuestion(preferencesPhaseKey, "539158b8e4b0b56e67d2c74b", "yes sir")) { _ =>
        status must_== 200
        modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(removeHakutoive) { hakemus =>
          status must_== 200
          withSavedApplication(hakemus) { application =>
            application.getPhaseAnswers(preferencesPhaseKey).containsKey("539158b8e4b0b56e67d2c74b") must_== false
          }
        }
      }
    }

    "apply hiddenValues on the form" in {
      def removeDiscretionaryFlags(hakemus: Hakemus) = {
        // remove the discretionary flag to be able to test that it is automatically applied from the form
        hakemus.copy(hakutoiveet = hakemus.hakutoiveet.map {hakutoive =>
          hakutoive - "discretionary"
        })
      }
      modifyHakemus(hakemusYhteishakuKevat2014WithForeignBaseEducationId)(removeDiscretionaryFlags) { hakemus =>
        status must_== 200
        withSavedApplication(hakemus) { application =>
          // verify that the discretionary flag is actually set
          application.getPhaseAnswers(preferencesPhaseKey).get("preference1-discretionary") must_== "true"
          // verify that the flag is not set in a different phase (koulutustausta). this verifies a bug fix
          application.getPhaseAnswers("koulutustausta").get("preference1-discretionary") must_== null
        }
      }
    }


    "reject answers to unknown questions" in {
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(answerExtraQuestion(preferencesPhaseKey, "unknown", "hacking")) { hakemus =>
        status must_== 400
      }
    }

    "reject update of application with invalid application period" in {
      modifyHakemus(inactiveHakemus)((hakemus) => hakemus) { hakemus =>
          status must_== 403
      }
    }

    "reject update of application that is not ACTIVE or INCOMPLETE" in {
      setupFixture("submittedApplication")
      modifyHakemus(hakemusYhteishakuKevat2014WithForeignBaseEducationId)((hakemus) => hakemus) { hakemus =>
        status must_== 403
      }
    }

    "reject update of application that is in post processing" in {
      setupFixture("postProcessingFailed")
      modifyHakemus(hakemusYhteishakuKevat2014WithForeignBaseEducationId)((hakemus) => hakemus) { hakemus =>
        status must_== 403
      }
    }
  }
}
