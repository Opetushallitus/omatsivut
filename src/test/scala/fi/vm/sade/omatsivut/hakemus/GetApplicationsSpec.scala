package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.{PersonOid, TimeWarp}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GetApplicationsSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {

  val personOidWithAtaru = "1.2.246.562.24.14229104473"

  sequential

  "GET /applications" should {
    "return person's applications" in {
      withApplicationsResponse { resp =>
        resp.allApplicationsFetched must_== true
        resp.applications.map(_.hakemus.oid) must contain(hakemusNivelKesa2013WithPeruskouluBaseEducationId)
        resp.applications.map(_.hakemus.oid) must contain(hakemusYhteishakuKevat2014WithForeignBaseEducationId)
      }
    }

    "return person's applications from ataru" in {
      withApplicationsResponse { resp =>
        resp.applications(0).hakemus.oid must_== "1.2.246.562.11.WillNotBeFoundInTarjonta"
        resp.applications(0).hakemusSource must_== "Ataru"
        resp.applications(0).hakemus.ohjeetUudelleOpiskelijalle("1.2.246.562.20.14660127086") must_== "https://www.helsinki.fi/fi/opiskelu/ohjeita-hakemuksen-jattaneille-yhteishaku"
      }(PersonOid(personOidWithAtaru))
    }

    "contain the link to 'information for new students'" in {
      withApplicationsResponse { resp =>
        val hakukohdeOid = "1.2.246.562.20.14660127086"
        resp.applications(6).hakemus.ohjeetUudelleOpiskelijalle(hakukohdeOid) must_== // we are going to have map hakukohdeOid -> link to instructions
          "https://www.helsinki.fi/fi/opiskelu/ohjeita-hakemuksen-jattaneille-yhteishaku"
      }
    }

    "tell for basic application that no additional info is required" in {
      withHakemusWithEmptyAnswers(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemusInfo =>
        hakemusInfo.hakemus.requiresAdditionalInfo must_== false
      }
    }

    "tell for dance education application that additional info is required" in {
      withHakemusWithEmptyAnswers(TestFixture.hakemusWithGradeGridAndDancePreference) { hakemusInfo =>
        hakemusInfo.hakemus.requiresAdditionalInfo must_== true
      }
    }

    "tell for discretionary application that additional info is required" in {
      withHakemusWithEmptyAnswers(hakemusYhteishakuKevat2014WithForeignBaseEducationId) { hakemusInfo =>
        hakemusInfo.hakemus.requiresAdditionalInfo must_== true
      }
    }

    "use application system's application period when application type is not 'LISÄHAKU'" in {
      withHakemusWithEmptyAnswers(hakemusYhteishakuKevat2014WithForeignBaseEducationId) { hakemusInfo =>
        hakemusInfo.hakemus.haku.applicationPeriods.head must_== TestFixture.hakemus2_hakuaika
      }
    }
    "provide additional application period for application with athlete questions" in {
      withHakemusWithEmptyAnswers(hakemusWithAtheleteQuestions) { hakemusInfo =>
        hakemusInfo.hakemus.hakutoiveet.head.kohdekohtainenHakuaika match {
          case Some(aika) =>
            aika.start must_== 1404290831839L
            aika.end must_== 4507513600000L
          case _ => ko("Application period doesn't match")
        }
      }
    }
  }
}
