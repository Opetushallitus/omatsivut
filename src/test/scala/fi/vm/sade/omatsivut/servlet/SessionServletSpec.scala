package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestSupport}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.util.parsing.json.JSON

@RunWith(classOf[JUnitRunner])
class SessionServletSpec extends ScalatraTestSupport {
  sequential

  implicit val personOid: PersonOid = PersonOid("dummy")

  "GET /session" should {

    "fail with authentication error, if session does not exist" in {
      deleteAllSessions
      get("session/") {
        status must_== 401
      }
    }

    "return json containing user's display name and birthday, if session does exist" in {
      authGet("session/") {
        status must_== 200
        val result = JSON.parseFull(body)
        result must not(beNone)
        val resultMap: Map[String, String] = result.getOrElse(Map()).asInstanceOf[Map[String, String]]
        resultMap("name") must_== "John Smith"
      }
    }
  }
}
