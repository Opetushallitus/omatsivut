package ohp

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class PreviewSimulation extends Simulation {

	val httpProtocol = http
		.baseURL("http://localhost:7337")
		.inferHtmlResources(BlackList(""".*\.js""", """.*css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png"""), WhiteList())
		.acceptHeader("""text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")
		.acceptEncodingHeader("""gzip, deflate""")
		.acceptLanguageHeader("""en-US,en;q=0.5""")
		.connection("""keep-alive""")
		.contentTypeHeader("""application/json;charset=utf-8""")

	val headers_4 = Map("""Accept""" -> """application/json, text/plain, */*""")

	val headers_5 = Map(
		"""Accept""" -> """application/json, text/plain, */*""",
		"""Cache-Control""" -> """no-cache""",
		"""Pragma""" -> """no-cache""")

    val scn = scenario("PreviewSimulation")
		.exec(http("Login")
			.get("""/omatsivut/login"""))
		.pause(2)
		.exec(http("Get applications")
			.get("""/omatsivut/Shibboleth.sso/fakesession?hetu=010101-123N""")
			.resources(http("request_2")
			.get( """/omatsivut/secure/applications""")
			.headers(headers_4)))
		.pause(7)
		.exec(http("Preview one application")
			.get("""/omatsivut/secure/applications/preview/1.2.246.562.11.00000441368"""))
		.pause(4)
		.exec(http("Logout")
			.get("""/omatsivut/logout""")
			.resources(http("request_11")
			.get( """/omatsivut/secure/applications""")
			.headers(headers_4)
			.check(status.is(401))))

	setUp(scn.inject(rampUsers(20) over (10 seconds))).protocols(httpProtocol)
}