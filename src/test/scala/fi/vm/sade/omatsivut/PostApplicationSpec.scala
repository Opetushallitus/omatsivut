package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.{Hakemus, ValidationError}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.servlet.{ApplicationsServlet, OmatSivutSwagger}
import org.json4s._
import org.json4s.native.{JsonMethods, Serialization}

class PostApplicationSpec extends JsonFormats with ScalatraTestSupport {
  "POST /application/unanswered" should {
    "find unanswered question from application" in {
      println("******************* 3")
      AppConfig.fromSystemProperty.withConfig {
        authGet("/applications", "1.2.246.562.24.14229104472") {
          val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
          val hakemus = applications(0)
          authPost("/applications/unanswered/" + hakemus.oid, "1.2.246.562.24.14229104472", Serialization.write(hakemus)) {
            val questions: JValue = JsonMethods.parse(body)
            val titles: List[String] = questions \\ "title" \\ "fi" \\ classOf[JString]
            questions.children.size must_== 2
            titles.head must_== "Päättötodistuksen kaikkien oppiaineiden keskiarvo?"
            status must_== 200
          }
        }
      }
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}