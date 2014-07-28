package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet

class GetApplicationsSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT

  sequential

  "GET /applications" should {
    "return person's applications" in {
      withApplications(personOid) { applications =>
        applications.map(_.oid) must contain(hakemus1)
        applications.map(_.oid) must contain(hakemus2)
      }
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}