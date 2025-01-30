package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.{PersonOid, TimeWarp}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GetApplicationsSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {

  val personOidWithAtaru = "1.2.246.562.24.14229104473"

  sequential

  "GET /applications" should {

    "return person's applications from ataru" in {
      withApplicationsResponse { resp =>
        resp.applications(0).hakemus.oid must_== "1.2.246.562.11.WillNotBeFoundInTarjonta"
        resp.applications(0).hakemusSource must_== "Ataru"
        resp.applications(0).hakemus.ohjeetUudelleOpiskelijalle("1.2.246.562.20.14660127086") must_== "https://www.helsinki.fi/fi/opiskelu/ohjeita-hakemuksen-jattaneille-yhteishaku"
      }(PersonOid(personOidWithAtaru))
    }

  }
}
