package fi.vm.sade.omatsivut.valintarekisteri

import javax.servlet.ServletContext

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization
import org.scalatra.{LifeCycle, Ok, ScalatraServlet}
import org.scalatra.json.JacksonJsonSupport

class MockedValintarekisteriBackEnd extends ScalatraServlet with JacksonJsonSupport with JsonFormats with Logging {

  before() {
    contentType = formats("json")
  }

  post("/vastaanotto") {
    val vastaanotto = Serialization.read[VastaanottoIlmoitus](request.body)
    println(s"got vastaanotto: $vastaanotto")
    Ok()
  }

}

class MockedValintarekisteriScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) {
    context mount (new MockedValintarekisteriBackEnd, "/*")
  }
}