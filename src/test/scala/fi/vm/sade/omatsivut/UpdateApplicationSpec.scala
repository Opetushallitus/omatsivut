package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s._
import org.json4s.jackson.{JsonMethods, Serialization}

class UpdateApplicationSpec extends JsonFormats with ScalatraTestSupport {
  override implicit lazy val appConfig = new AppConfig.IT
  sequential

  "PUT /application/:oid" should {
    "validate application" in {
      authGet("/applications", "1.2.246.562.24.14229104472") {
        val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
        val hakemus = applications(0)
        val henkiloTiedot: Map[String, String] = hakemus.answers.getOrElse("henkilotiedot", Map()) + ("Henkilotunnus" -> "010101-123N")
        val newHakemus = hakemus.copy(answers = hakemus.answers + ("henkilotiedot" -> henkiloTiedot))
        authPut("/applications/" + hakemus.oid, "1.2.246.562.24.14229104472", Serialization.write(newHakemus)) {
          val result: JValue = JsonMethods.parse(body)
          status must_== 200
          compareWithoutTimestamp(newHakemus, result.extract[Hakemus]) must_== true
        }
      }
    }
  }

  def compareWithoutTimestamp(hakemus1: Hakemus, hakemus2: Hakemus) = {
    hakemus1.copy(updated = hakemus2.updated) == hakemus2
  }

  addServlet(new ApplicationsServlet(), "/*")

}
