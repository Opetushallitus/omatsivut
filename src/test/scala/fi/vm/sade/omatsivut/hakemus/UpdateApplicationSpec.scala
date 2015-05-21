package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.hakemuseditori.hakemus.domain.{Hakutoive, Hakemus}
import fi.vm.sade.haku.oppija.hakemus.domain.{ApplicationNote, Change}
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.{PersonOid, TimeWarp}
import org.json4s._
import org.json4s.jackson.JsonMethods

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UpdateApplicationSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {
  sequential

  "PUT /application/:oid" should {
    "reject application with empty hakutoiveet" in {
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemus => hakemus.copy(hakutoiveet = Nil) } { _ =>
        status must_== 400
      }
    }

    "reject application with different person oid" in {
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemusInfo =>
        saveHakemus(hakemusInfo.hakemus) {
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
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(answerExtraQuestion(hakutoiveet, "539158b8e4b0b56e67d2c74b", "yes sir")) { newHakemus =>
        status must_== 200
        val result: JValue = JsonMethods.parse(body)
        hasSameHakuToiveet(newHakemus, result.extract[Hakemus]) must_== true
        // verify saved application
        withSavedApplication(newHakemus) { application =>
          application.getPhaseAnswers(henkilotiedot).get(henkilotunnus) must_== testHetu
          application.getPhaseAnswers(hakutoiveet).get("539158b8e4b0b56e67d2c74b") must_== "yes sir"
        }
      }
    }

    "update application change history" in {
      import scala.collection.JavaConversions._
      "single change" in {
        fixtureImporter.applyFixtures()
        val modification = answerExtraQuestion(hakutoiveet, "539158b8e4b0b56e67d2c74b", "yes sir") _
        modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(modification) { newHakemus =>
          status must_== 200
          withSavedApplication(newHakemus) { application =>
            application.getHistory.size must_== 2 // one existing change + new change prepended at position 0
          val change: Change = application.getHistory.get(0)
            change.getModifier must_== "oppija 1.2.246.562.24.14229104472"
            change.getReason must_== "Muokkaus omatsivut -palvelussa"
            change.getChanges.toList.map(_.toMap) must_== List(
              Map("field" -> "539158b8e4b0b56e67d2c74b", "old value" -> "En tiedä mihin muualle hakisin", "new value" -> "yes sir"),
              Map("field" -> "preference4-Opetuspiste-id", "new value" -> ""),
              Map("field" -> "preference5-Opetuspiste-id", "new value" -> ""),
              Map("field" -> "preference5-Koulutus-id", "new value" -> ""),
              Map("field" -> "preference4-Koulutus-id", "new value" -> ""),
              Map("field" -> "eligibility_1_2_246_562_14_2014032812530780195965", "new value" -> "NOT_CHECKED:UNKNOWN:null"),
              Map("field" -> "eligibility_1_2_246_562_14_2014032610154661183054", "new value" -> "NOT_CHECKED:UNKNOWN:null"),
              Map("field" -> "eligibility_1_2_246_562_14_2014040212501070122979", "new value" -> "NOT_CHECKED:UNKNOWN:null")
            )
            application.getNotes.size must_== 3 // two existing notes + new note at position 0
          val note: ApplicationNote = application.getNotes.get(0)
            note.getUser must_== "1.2.246.562.24.14229104472"
            note.getNoteText must_== "Hakija päivittänyt vaihetta 'hakutoiveet'"
          }
        }
      }
      "multiple changes" in {
        fixtureImporter.applyFixtures()
        val modification = answerExtraQuestion(hakutoiveet, "539158b8e4b0b56e67d2c74b", "yes sir") _ compose answerExtraQuestion(henkilotiedot, "lahiosoite", "Obama") _
        modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(modification) { newHakemus =>
          status must_== 200
          withSavedApplication(newHakemus) { application =>
          val note: ApplicationNote = application.getNotes.get(0)
            note.getUser must_== "1.2.246.562.24.14229104472"
            note.getNoteText must_== "Hakija päivittänyt vaiheita 'henkilotiedot', 'hakutoiveet'"
          }
        }
      }

    }

    "prune answers to removed questions" in {
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(answerExtraQuestion(hakutoiveet, "539158b8e4b0b56e67d2c74b", "yes sir")) { _ =>
        status must_== 200
        modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(removeHakutoive) { hakemus =>
          status must_== 200
          withSavedApplication(hakemus) { application =>
            application.getPhaseAnswers(hakutoiveet).containsKey("539158b8e4b0b56e67d2c74b") must_== false
          }
        }
      }
    }

    "apply hiddenValues on the form" in {
      fixtureImporter.applyFixtures()

      def removeDiscretionaryFlags(hakemus: Hakemus) = {
        // remove the discretionary flag to be able to test that it is automatically applied from the form
        hakemus.copy(hakutoiveet = hakemus.hakutoiveet.map { t =>
          t.copy(hakemusData = t.hakemusData.map(_ - "discretionary"))
        })
      }
      modifyHakemus(hakemusYhteishakuKevat2014WithForeignBaseEducationId)(removeDiscretionaryFlags) { hakemus =>
        status must_== 200
        withSavedApplication(hakemus) { application =>
          // verify that the discretionary flag is actually set
          application.getPhaseAnswers(hakutoiveet).get("preference1-discretionary") must_== "true"
          // verify that the flag is not set in a different phase (koulutustausta). this verifies a bug fix
          application.getPhaseAnswers("koulutustausta").get("preference1-discretionary") must_== null
        }
      }
    }


    "reject answers to unknown questions" in {
      modifyHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId)(answerExtraQuestion(hakutoiveet, "unknown", "hacking")) { hakemus =>
        status must_== 400
      }
    }

    "allow updating of contact info after application period end, but before application round end" in {
      modifyHakemus(inactiveHakemusWithApplicationRoundNotEndedId)(answerExtraQuestion(henkilotiedot, lahiosoite, "uusi osoite")) { hakemus =>
        status must_== 200
        withSavedApplication(hakemus) { application =>
          application.getPhaseAnswers(henkilotiedot).get(lahiosoite) must_== "uusi osoite"
        }
      }
    }

    "do not allow updating of other info after application period end, but before application round end" in {
      modifyHakemus(inactiveHakemusWithApplicationRoundNotEndedId)(addHakutoive(Hakutoive(Some(Map(
        "Koulutus-id" -> "1.2.246.562.20.60377543290",
        "Koulutus" -> "foobar",
        "Opetuspiste" -> "barfoo",
        "Opetuspiste-id" -> "1.2.246.562.20.60377543290",
        "Koulutus-id-lang" -> "FI",
        "Koulutus-id-aoIdentifier" -> "019"))))) { hakemus =>
        status must_== 403
      }
    }

    "reject update of application after application round end" in {
      modifyHakemus(inactiveHakemusWithApplicationRoundEndedId)(answerExtraQuestion(henkilotiedot, lahiosoite, "uusi osoite2")) { hakemus =>
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

    "allow reordering application preferences if application period is active" in {
      setupFixture(hakemusKorkeakoulutKevat2014Id)
      setApplicationStart(hakemusKorkeakoulutKevat2014Id, 0)
      modifyHakemus (hakemusKorkeakoulutKevat2014Id){ hakemus =>
        hakemus.copy(hakutoiveet = List(hakemus.hakutoiveet(1), hakemus.hakutoiveet(0)) ++ hakemus.hakutoiveet.slice(2, hakemus.hakutoiveet.length))
      } { hakemus =>
        status must_== 200
      }
    }

    "reject reordering application preferences if application period has passed" in {
      setupFixture(hakemusKorkeakoulutKevat2014Id)
      setApplicationStart(hakemusKorkeakoulutKevat2014Id, -70)
      modifyHakemus (hakemusKorkeakoulutKevat2014Id){ hakemus =>
        hakemus.copy(hakutoiveet = hakemus.hakutoiveet.slice(0,3).reverse ++ hakemus.hakutoiveet.slice(3, hakemus.hakutoiveet.length))
      } { hakemus =>
        status must_== 400
      }
    }

    "reject removing application preferences if application period has passed" in {
      setupFixture(hakemusKorkeakoulutKevat2014Id)
      setApplicationStart(hakemusKorkeakoulutKevat2014Id, -70)
      modifyHakemus (hakemusKorkeakoulutKevat2014Id){ hakemus =>
        hakemus.copy(hakutoiveet = hakemus.hakutoiveet.tail)
      } { hakemus =>
        status must_== 400
      }
    }

    "reject reordering application preferences if hakutoive specific application period has passed" in {
      setupFixture(hakemusErityisopetuksenaId)
      setApplicationStart(hakemusErityisopetuksenaId, 0)
      modifyHakemus (hakemusErityisopetuksenaId) { hakemus =>
        hakemus.copy(hakutoiveet = hakemus.hakutoiveet.slice(0,2).reverse ++ hakemus.hakutoiveet.slice(2, hakemus.hakutoiveet.length))
      } { hakemus =>
        status must_== 400
      }
    }

    "reject removing application preferences if hakutoive specific application period has passed" in {
      setupFixture(hakemusErityisopetuksenaId)
      setApplicationStart(hakemusErityisopetuksenaId, 0)
      modifyHakemus (hakemusErityisopetuksenaId) { hakemus =>
        hakemus.copy(hakutoiveet = hakemus.hakutoiveet.tail)
      } { hakemus =>
        status must_== 400
      }
    }

    "allow changing contact info if hakutoive specific application period has passed" in {
      setupFixture(hakemusErityisopetuksenaId)
      setApplicationStart(hakemusErityisopetuksenaId, 0)
      modifyHakemus (hakemusErityisopetuksenaId) (answerExtraQuestion(henkilotiedot, lahiosoite, "uusi osoite")) { hakemus =>
        status must_== 200
      }
    }
  }
}
