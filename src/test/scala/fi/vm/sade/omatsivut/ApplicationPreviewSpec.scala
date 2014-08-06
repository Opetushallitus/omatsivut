package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s.jackson.Serialization

class ApplicationPreviewSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT

  sequential

  "GET /applications/preview/:oid" should {
    "generate application preview" in {
      FixtureImporter().applyOverrides("peruskoulu")
      authGet("/applications/preview/" + TestFixture.hakemus2, personOid) {
        val prettier = new scala.xml.PrettyPrinter(80, 4)
        println(prettier.format(scala.xml.XML.loadString(body)))
        success
      }
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}
