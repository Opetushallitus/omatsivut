package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
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

    "tell for basic application that no additional info is required" in {
      withHakemus(TestFixture.hakemus1) { hakemus =>
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
      withHakemus(TestFixture.hakemus2) { hakemus =>
        hakemus.requiresAdditionalInfo must_== true
      }
    }

    "use application system's application period when application type is not 'LISÄHAKU'" in {
      withHakemus(TestFixture.hakemus2) { hakemus =>
        hakemus.haku.applicationPeriods.head must_== TestFixture.hakemus2_hakuaika
      }
    }

    "use preference's application period when application type is 'LISÄHAKU'" in {
      withHakemus(TestFixture.hakemusLisahaku) { hakemus =>
        hakemus.haku.applicationPeriods.head must_== TestFixture.hakemusLisahaku_hakuaikaForPreference
      }
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}