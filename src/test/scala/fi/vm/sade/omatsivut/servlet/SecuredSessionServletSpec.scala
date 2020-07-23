package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.AttributeNames
import fi.vm.sade.omatsivut.{ScalatraTestCookiesSupport, ScalatraTestSupport}
import org.junit.runner.RunWith
import org.slf4j.LoggerFactory
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class SecuredSessionServletSpec extends ScalatraTestSupport with AttributeNames with ScalatraTestCookiesSupport {
  val urlUsedByCAS = "initsession"
  private val logger = LoggerFactory.getLogger(getClass)

  "GET /initsession" should {
    "fails with bad request (400) if request does not contain ticket" in {
      get(urlUsedByCAS) {
        status must_== 400
        body must contain("No ticket found from CAS request")
      }
    }

    "create a session in repository and forwards to root if the request contains hetu header" in {
      get(urlUsedByCAS, params = Map("ticket" -> TestFixture.testCASticket)) {
        println("ASDASSDASD")
        println(response.body)
        status must_== 302
        val location = response.headers("Location").head
        location must endWith("omatsivut/index.html")
        val sessionId = cookieGetValue(response, sessionCookieName).getOrElse("not found session cookie")
        val personOid = getPersonFromSession(sessionId).getOrElse("not found in repository")
        logger.info(s"response: $response")
        logger.info(s"location: $location")
        logger.info(s"sessionId: $sessionId")
        logger.info(s"personOid: $personOid")
        personOid must_== TestFixture.personOid

        get("logout", headers = Map("Cookie" -> s"$sessionCookieName=$sessionId")) {
          response.status must_== 302
          response.getHeader("Location") must endWith("/omatsivut/Shibboleth.sso/Logout?return=%2Fkoski%2Fuser%2Flogout")
        }
      }
    }

    "create a session with no oid if hetu does not have the corresponding oid" in {
      get(urlUsedByCAS, params = Map("ticket" -> TestFixture.testCASticketWithNoPersonOid)) {
        println("ASDASSDASD")
        println(response.body)
        status must_== 302
        val location = response.headers("Location").head
        location must endWith("omatsivut/index.html")
        val sessionId = cookieGetValue(response, sessionCookieName).getOrElse("not found session cookie")
        val personOid = getPersonFromSession(sessionId).getOrElse("not found in repository")
        personOid must_== ""

        get("logout", headers = Map("Cookie" -> s"$sessionCookieName=$sessionId")) {
          response.status must_== 302
          response.getHeader("Location") must endWith("/omatsivut/Shibboleth.sso/Logout?return=%2Fkoski%2Fuser%2Flogout")
        }
      }
    }

    "create a session in repository, and it will contain also the display name of the user" in {
      val firstName = "Wolfgang"
      val secondName = "Mozart"
      get(urlUsedByCAS, params = Map("ticket" -> TestFixture.testCASticket, "firstname" -> firstName, "sn" -> secondName)) {  // TODO: firstname ja sn eivät taatusti toimi tässä
        println("ASDASSDASD")
        println(response.body)
        status must_== 302
        val sessionId = cookieGetValue(response, sessionCookieName).getOrElse("not found session cookie")
        val displayName = getDisplayNameFromSession(sessionId).getOrElse("not found in repository")
        displayName must_== firstName + " " + secondName

        get("logout", headers = Map("Cookie" -> s"$sessionCookieName=$sessionId")) {
          response.status must_== 302
          response.getHeader("Location") must endWith("/omatsivut/Shibboleth.sso/Logout?return=%2Fkoski%2Fuser%2Flogout")
        }
      }
    }
  }
}
