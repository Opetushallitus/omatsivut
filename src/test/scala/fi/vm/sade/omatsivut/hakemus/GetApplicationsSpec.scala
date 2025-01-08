package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.{PersonOid, TimeWarp}
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GetApplicationsSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {

  val personOidWithAtaru = "1.2.246.562.24.14229104473"

  sequential

  "GET /applications" should {
//    TODO fix fai delete?
//    "return person's applications" in {
//      withApplicationsResponse { resp =>
//        resp.allApplicationsFetched must_== true
//        resp.applications.map(_.hakemus.oid) must contain(hakemusNivelKesa2013WithPeruskouluBaseEducationId)
//        resp.applications.map(_.hakemus.oid) must contain(hakemusYhteishakuKevat2014WithForeignBaseEducationId)
//      }
//    }

    "return person's applications from ataru" in {
      withApplicationsResponse { resp =>
        resp.applications(0).hakemus.oid must_== "1.2.246.562.11.WillNotBeFoundInTarjonta"
        resp.applications(0).hakemusSource must_== "Ataru"
        resp.applications(0).hakemus.ohjeetUudelleOpiskelijalle("1.2.246.562.20.14660127086") must_== "https://www.helsinki.fi/fi/opiskelu/ohjeita-hakemuksen-jattaneille-yhteishaku"
      }(PersonOid(personOidWithAtaru))
    }

  }
}
