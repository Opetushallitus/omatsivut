package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestSupport}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.json4s._
import org.json4s.jackson.JsonMethods

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class User(name: String, birthDay: Option[LocalDate])

@RunWith(classOf[JUnitRunner])
class SessionServletSpec extends ScalatraTestSupport {
  sequential

  implicit val formats: Formats = DefaultFormats + new LocalDateSerializer

  class LocalDateSerializer extends CustomSerializer[LocalDate](_ => (
    {
      case JString(s) => LocalDate.parse(s, DateTimeFormatter.ISO_DATE)
      case JNull      => null
    },
    {
      case date: LocalDate => JString(date.format(DateTimeFormatter.ISO_DATE))
    }
  ))

  def getNameFromJsonBody(body: String): String = {
    val user = JsonMethods.parse(body).extract[User]
    user.name
  }
  "GET /session" should {

    "fail with authentication error, if session does not exist" in {
      deleteAllSessions
      get("session/") {
        status must_== 401
      }
    }

    "return json containing user's display name and birthday, if session does exist" in {
      implicit val personOid: PersonOid = PersonOid("1.2.3.4.5.6")
      authGet("session/") {
        status must_== 200
        getNameFromJsonBody(body) must_== "John Smith"
      }
    }

    "return json containing user's display name and birthday, if session does exist, even if oid is not in the session" in {
      implicit val personOid: PersonOid = PersonOid("")
      authGet("session/") {
        status must_== 200
        getNameFromJsonBody(body) must_== "John Smith"
      }
    }
  }
}
