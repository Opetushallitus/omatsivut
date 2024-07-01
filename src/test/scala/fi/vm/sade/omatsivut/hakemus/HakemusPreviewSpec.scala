package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class HakemusPreviewSpec extends HakemusApiSpecification with FixturePerson {

  sequential

  "GET /secure/applications/preview/:oid" should {

    "always reject access" in {
      authGet("secure/applications/preview/" + hakemusYhteishakuKevat2014WithForeignBaseEducationId) {
        response.status must_== 403
      }
    }

  }

}
