package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.TimeWarp
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.hakemuseditori.lomake.domain.QuestionNode
import org.joda.time.DateTime
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GetApplicationsSpec extends HakemusApiSpecification with FixturePerson with TimeWarp {

  sequential

  "GET /applications" should {
    "return person's applications" in {
      withApplications { applications =>
        applications.map(_.hakemus.oid) must contain(hakemusNivelKesa2013WithPeruskouluBaseEducationId)
        applications.map(_.hakemus.oid) must contain(hakemusYhteishakuKevat2014WithForeignBaseEducationId)
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

    "do not return questions and errors for inactive applications" in {
      withHakemusWithEmptyAnswers(hakemusWithAtheleteQuestions) { hakemusInfo =>
        hakemusInfo.errors must_== List()
        QuestionNode.flatten(hakemusInfo.questions).map(_.title).length must_== 21
        withFixedDateTime(DateTime.now().plusYears(100).getMillis) {
          withHakemusWithEmptyAnswers(hakemusWithAtheleteQuestions) { hakemusInfo =>
            hakemusInfo.errors must_== List()
            hakemusInfo.questions must_== List()
          }
        }
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