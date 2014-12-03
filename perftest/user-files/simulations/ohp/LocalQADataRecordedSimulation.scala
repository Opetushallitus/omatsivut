package ohp

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class LocalQADataRecordedSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("http://localhost:8080")
		.inferHtmlResources(BlackList(""".*css\?.*""", """.*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())
		.acceptHeader("""application/json, text/plain, */*""")
		.acceptEncodingHeader("""gzip, deflate""")
		.acceptLanguageHeader("""en-US,en;q=0.5""")
		.connection("""keep-alive""")
		.contentTypeHeader("""application/json;charset=utf-8""")
		.userAgentHeader("""Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:29.0) Gecko/20100101 Firefox/29.0""")

	val headers_0 = Map(
		"""Accept""" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
		"""Cache-Control""" -> """max-age=0""")

	val headers_5 = Map("""Accept""" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")

	val headers_10 = Map(
		"""Cache-Control""" -> """no-cache""",
		"""Pragma""" -> """no-cache""")

	val scn = scenario("LocalQADataRecordedSimulation").repeat(1) {
    exec(http("Get front page")
      .get( """/omatsivut/""")
      .headers(headers_0))
      .exec(http("API call on front page. Check that api is inaccessible")
      .get( """/omatsivut/secure/applications""")
      .resources(http("Login request")
      .get( """/omatsivut/login""")
      .headers(headers_5))
      .check(status.is(401)))
      .exec(http("Fake login call")
      .get( """/omatsivut/util/Shibboleth.sso?hetu=210281-9988""")
      .headers(headers_5))
      .exec(http("Get application page")
      .get( """/omatsivut/secure/applications"""))
      .exec(http("Validate application change move 2nd item to top")
      .post( """/omatsivut/secure/applications/validate/1.2.246.562.11.00000943332""")
      .headers(headers_10)
      .body(RawFileBody("FirstValidationRequest-ChangePreference.txt")))
      .exec(http("Validate application change move top item to 2nd")
      .post( """/omatsivut/secure/applications/validate/1.2.246.562.11.00000943332""")
      .headers(headers_10)
      .body(RawFileBody("SecondValidationRequest-ChangePreference.txt")))
      .exec(http("Save application")
      .put("""/omatsivut/secure/applications/1.2.246.562.11.00000943332""")
      .body(RawFileBody("SaveApplicationRequest.txt")))
      .exec(http("Preview application")
      .get( """/omatsivut/secure/applications/preview/1.2.246.562.11.00000943332""")
      .headers(headers_5))
      .exec(http("Logout request")
      .get( """/omatsivut/logout""")
      .headers(headers_5)
      .resources(http("Front page request, check that API is inaccessible after logout")
      .get( """/omatsivut/secure/applications""")
      .check(status.is(401)),
        http("User redirected to login page after logout")
          .get( """/omatsivut/login""")
          .headers(headers_5)))
  }

	setUp(scn.inject(atOnceUsers(1))).protocols(httpProtocol)
}