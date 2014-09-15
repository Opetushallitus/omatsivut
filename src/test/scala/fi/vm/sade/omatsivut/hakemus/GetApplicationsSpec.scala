package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._

class GetApplicationsSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT

  sequential

  "GET /applications" should {
    "return person's applications" in {
      withApplications(personOid) { applications =>
        applications.map(_.oid) must contain(hakemusNivelKesa2013WithPeruskouluBaseEducationId)
        applications.map(_.oid) must contain(hakemusYhteishakuKevat2014WithForeignBaseEducationId)
      }
    }

    "tell for basic application that no additional info is required" in {
      withHakemus(hakemusNivelKesa2013WithPeruskouluBaseEducationId) { hakemus =>
        hakemus.requiresAdditionalInfo must_== false
      }
    }

    "tell for higher level attachments that additional info is required" in {
      withHakemus(TestFixture.hakemusWithHigherGradeAttachments) { hakemus =>
        hakemus.requiresAdditionalInfo must_== true
      }
    }

    "tell for dance education application that additional info is required" in {
      withHakemus(TestFixture.hakemusWithGradeGridAndDancePreference) { hakemus =>
        hakemus.requiresAdditionalInfo must_== true
      }
    }

    "tell for discretionary application that additional info is required" in {
      withHakemus(hakemusYhteishakuKevat2014WithForeignBaseEducationId) { hakemus =>
        hakemus.requiresAdditionalInfo must_== true
      }
    }

    "use application system's application period when application type is not 'LISÄHAKU'" in {
      withHakemus(hakemusYhteishakuKevat2014WithForeignBaseEducationId) { hakemus =>
        hakemus.haku.applicationPeriods.head must_== TestFixture.hakemus2_hakuaika
      }
    }

    "use preference's application period when application type is 'LISÄHAKU'" in {
      withHakemus(TestFixture.hakemusLisahaku) { hakemus =>
        hakemus.haku.applicationPeriods.head must_== TestFixture.hakemusLisahaku_hakuaikaForPreference
      }
    }
  }

  addServlet(appConfig.componentRegistry.newApplicationsServlet, "/*")
}