//package fi.vm.sade.omatsivut.servlet
//
//import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestSupport}
//import org.junit.runner.RunWith
//import org.specs2.runner.JUnitRunner
//
//import scala.util.parsing.json.JSON
//
//@RunWith(classOf[JUnitRunner])
//class SessionServletSpec extends ScalatraTestSupport {
//  sequential
//
//  def getNameFromJsonBody(body: String): String = {
//    val result = JSON.parseFull(body)
//    result must not(beNone)
//    val resultMap: Map[String, String] = result.getOrElse(Map()).asInstanceOf[Map[String, String]]
//    resultMap("name")
//  }
//
//  "GET /session" should {
//
//    "fail with authentication error, if session does not exist" in {
//      deleteAllSessions
//      get("session/") {
//        status must_== 401
//      }
//    }
//
//    "return json containing user's display name and birthday, if session does exist" in {
//      implicit val personOid: PersonOid = PersonOid("1.2.3.4.5.6")
//      authGet("session/") {
//        status must_== 200
//        getNameFromJsonBody(body) must_== "John Smith"
//      }
//    }
//
//    "return json containing user's display name and birthday, if session does exist, even if oid is not in the session" in {
//      implicit val personOid: PersonOid = PersonOid("")
//      authGet("session/") {
//        status must_== 200
//        getNameFromJsonBody(body) must_== "John Smith"
//      }
//    }
//  }
//}
