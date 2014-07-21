package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class UpdateApplicationSpec extends JsonFormats with ScalatraTestSupport {
  override implicit lazy val appConfig = new AppConfig.IT
  val testApplicationOid: String = "1.2.246.562.24.14229104472"
  val personalInfoPhaseKey: String = OppijaConstants.PHASE_PERSONAL
  val preferencesPhaseKey: String = OppijaConstants.PHASE_APPLICATION_OPTIONS
  val skillsetPhaseKey: String = OppijaConstants.PHASE_GRADES
  val ssnKey: String = OppijaConstants.ELEMENT_ID_SOCIAL_SECURITY_NUMBER
  val testSsn: String = "010101-123N"
  sequential

  "PUT /application/:oid" should {
    "validate application" in {
      authGet("/applications", testApplicationOid) {
        val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
        val hakemus = applications(0)
        val henkiloTiedot: Map[String, String] = hakemus.answers.getOrElse(personalInfoPhaseKey, Map()) + (ssnKey -> testSsn)
        val newHakemus = hakemus.copy(answers = hakemus.answers + (personalInfoPhaseKey -> henkiloTiedot))
        authPut("/applications/" + hakemus.oid, testApplicationOid, Serialization.write(newHakemus)) {
          val result: JValue = JsonMethods.parse(body)
          status must_== 200
          compareWithoutTimestamp(newHakemus, result.extract[Hakemus]) must_== true
        }
      }
    }
  }

  "PUT /application/:oid" should {
    "save application" in {
      authGet("/applications", testApplicationOid) {
        val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
        val hakemus = applications(0)
        val personalInfo: (String, Map[String, String]) = personalInfoPhaseKey -> Map(ssnKey -> testSsn)
        val preferences: (String, Map[String, String]) = preferencesPhaseKey -> Map("hakutoiveet-testikysymys" -> "hakutoiveet-testivastaus")
        val skillset: (String, Map[String, String]) = skillsetPhaseKey -> Map("osaaminen-testikysymys" -> "osaaminen-testivastaus")
        val newHakemus = hakemus.copy(answers = hakemus.answers + personalInfo + preferences + skillset)
        authPut("/applications/" + hakemus.oid, testApplicationOid, Serialization.write(newHakemus)) {
          val application = appConfig.springContext.applicationDAO.find(new Application().setOid(hakemus.oid)).get(0)
          application.getPhaseAnswers(personalInfoPhaseKey).get(ssnKey) must_== testSsn
          application.getPhaseAnswers(preferencesPhaseKey).get("hakutoiveet-testikysymys") must_== "hakutoiveet-testivastaus"
          application.getPhaseAnswers(skillsetPhaseKey).get("osaaminen-testikysymys") must_== "osaaminen-testivastaus"
        }
      }
    }
  }

  def compareWithoutTimestamp(hakemus1: Hakemus, hakemus2: Hakemus) = {
    hakemus1.copy(updated = hakemus2.updated) == hakemus2
  }

  addServlet(new ApplicationsServlet(), "/*")

}
