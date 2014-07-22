package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class UpdateApplicationSpec extends JsonFormats with ScalatraTestSupport {
  override implicit lazy val appConfig = new AppConfig.IT
  val personalInfoPhaseKey: String = OppijaConstants.PHASE_PERSONAL
  val preferencesPhaseKey: String = OppijaConstants.PHASE_APPLICATION_OPTIONS
  val skillsetPhaseKey: String = OppijaConstants.PHASE_GRADES
  val ssnKey: String = OppijaConstants.ELEMENT_ID_SOCIAL_SECURITY_NUMBER
  val testSsn: String = "010101-123N"
  sequential
  
  addServlet(new ApplicationsServlet(), "/*")

  "PUT /application/:oid" should {
    "reject application with empty hakutoiveet" in {
      modifyHakemus { hakemus => withSsn(hakemus.copy(hakutoiveet = Nil)) } { _ =>
        status must_== 400
      }
    }

    "reject application with missing SSN" in {
      modifyHakemus { hakemus => hakemus } { _ =>
        status must_== 400
      }
    }


    "accept valid application" in {
      modifyHakemus { hakemus => withSsn(hakemus)} { hakemus =>
        val result: JValue = JsonMethods.parse(body)
        status must_== 200
        compareWithoutTimestamp(withSsn(hakemus), result.extract[Hakemus]) must_== true
      }
    }

    "save application" in {
      modifyHakemus(answerExtraQuestion(preferencesPhaseKey, "539158b8e4b0b56e67d2c74b", "yes sir")) { newHakemus =>
        status must_== 200
        val result: JValue = JsonMethods.parse(body)
        compareWithoutTimestamp(newHakemus, result.extract[Hakemus]) must_== true
        // verify saved application
        withApplication(newHakemus) { application =>
          application.getPhaseAnswers(personalInfoPhaseKey).get(ssnKey) must_== testSsn
          application.getPhaseAnswers(preferencesPhaseKey).get("539158b8e4b0b56e67d2c74b") must_== "yes sir"
        }
      }
    }

    "prune answers to removed questions" in {
      modifyHakemus(answerExtraQuestion(preferencesPhaseKey, "539158b8e4b0b56e67d2c74b", "yes sir")) { _ =>
        modifyHakemus(removeHakutoive) { hakemus =>
          withApplication(hakemus) { application =>
            application.getPhaseAnswers(preferencesPhaseKey).containsKey("539158b8e4b0b56e67d2c74b") must_== false
          }
        }
      }
    }

    "ignore answers to unknown questions" in {
      modifyHakemus(answerExtraQuestion(preferencesPhaseKey, "unknown", "hacking")) { hakemus =>
        withApplication(hakemus) { application =>
          application.getPhaseAnswers(preferencesPhaseKey).containsKey("unknown") must_== true // <-- TODO: should be false
        }
      }
    }
  }

  def withHakemus[T](f: (Hakemus => T)): T = {
    authGet("/applications", personOid) {
      val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
      val hakemus = applications(0)
      f(hakemus)
    }
  }

  def saveHakemus[T](hakemus: Hakemus)(f: => T): T = {
    authPut("/applications/" + hakemus.oid, personOid, Serialization.write(hakemus)) {
      f
    }
  }

  def withSsn(hakemus: Hakemus) = {
    val henkiloTiedot: Map[String, String] = hakemus.answers.getOrElse(personalInfoPhaseKey, Map()) + (ssnKey -> testSsn)
    hakemus.copy(answers = hakemus.answers + (personalInfoPhaseKey -> henkiloTiedot))
  }

  def modifyHakemus[T](modification: (Hakemus => Hakemus))(f: Hakemus => T): T = {
    withHakemus { hakemus =>
      val modified = modification(hakemus)
      saveHakemus(modified) {
        f(modified)
      }
    }
  }

  def answerExtraQuestion(phaseId: String, questionId: String, answer: String)(hakemus: Hakemus) = {
    val answerToExtraQuestion: Answers = Map(phaseId -> Map(questionId -> answer))
    withSsn(hakemus.copy(answers = hakemus.answers ++ answerToExtraQuestion))
  }

  def removeHakutoive(hakemus: Hakemus) = {    val answerToExtraQuestion: Answers = Map(preferencesPhaseKey -> Map("539158b8e4b0b56e67d2c74b" -> "yes sir"))
    withSsn(hakemus.copy(hakutoiveet = hakemus.hakutoiveet.slice(0, 2)))
  }

  def withApplication[T](hakemus: Hakemus)(f: Application => T): T = {
    val application = appConfig.springContext.applicationDAO.find(new Application().setOid(hakemus.oid)).get(0)
    f(application)
  }

  def compareWithoutTimestamp(hakemus1: Hakemus, hakemus2: Hakemus) = {
    hakemus1.copy(updated = hakemus2.updated) == hakemus2
  }
}
