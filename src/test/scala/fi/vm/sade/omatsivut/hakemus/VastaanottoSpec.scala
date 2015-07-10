package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.PersonOid
import fi.vm.sade.omatsivut.servlet.ClientSideVastaanotto
import org.json4s.jackson._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class VastaanottoSpec extends HakemusApiSpecification with FixturePerson {
  sequential

  "POST /applications/vastaanota/:hakuOid/:hakemusOid" should {
    "vastaanottaa paikan" in {
      authPost("secure/applications/vastaanota/1.2.246.562.5.2013080813081926341928/1.2.246.562.11.00000441369", Serialization.write(ClientSideVastaanotto("1.2.246.562.5.72607738902", "VASTAANOTTANUT"))) {
        status must_== 200
      }
    }

    "hylkää pyynnön väärältä henkilöltä" in {
      authPost("secure/applications/vastaanota/1.2.246.562.5.2013080813081926341928/1.2.246.562.11.00000441369", Serialization.write(ClientSideVastaanotto("1.2.246.562.5.72607738902", "VASTAANOTTANUT"))) {
        status must_== 404
      }(PersonOid("WRONG PERSON"))
    }
  }
}
