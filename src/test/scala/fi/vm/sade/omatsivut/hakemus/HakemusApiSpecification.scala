package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.json.{QuestionNodeSerializer, JsonFormats}
import org.json4s.JsonAST.JObject
import org.json4s._
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.{OmatSivutSpringContext, AppConfig}
import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestSupport}
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.FixtureImporter
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.hakemus.domain.{Hakutoive, Active, HakemuksenTila, Hakemus}
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus.{Answers, HakutoiveData}
import fi.vm.sade.omatsivut.json.JsonFormats
import org.json4s.jackson.Serialization
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import org.json4s.reflect.TypeInfo

trait HakemusApiSpecification extends ScalatraTestSupport {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats ++ List(new HakemuksenTilaSerializer)

  private val springContext: OmatSivutSpringContext = componentRegistry.springContext
  private val dao: ApplicationDAO = springContext.applicationDAO

  val personalInfoPhaseKey: String = OppijaConstants.PHASE_PERSONAL
  val preferencesPhaseKey: String = OppijaConstants.PHASE_APPLICATION_OPTIONS
  val skillsetPhaseKey: String = OppijaConstants.PHASE_GRADES
  val ssnKey: String = OppijaConstants.ELEMENT_ID_SOCIAL_SECURITY_NUMBER
  val addressKey: String = OppijaConstants.ELEMENT_ID_FIN_ADDRESS

  def withHakemus[T](oid: String)(f: (Hakemus => T))(implicit personOid: PersonOid): T = {
    withApplications { applications =>
      val hakemus = applications.find(_.oid == oid).get.copy(answers = Hakemus.emptyAnswers)
      f(hakemus)
    }
  }

  def withApplications[T](f: (List[Hakemus] => T))(implicit personOid: PersonOid): T = {
    authGet("secure/applications") {
      val b = body
      val applications: List[Hakemus] = Serialization.read[List[Hakemus]](b)
      f(applications)
    }
  }

  def saveHakemus[T](hakemus: Hakemus)(f: => T)(implicit personOid: PersonOid): T = {
    authPut("secure/applications/" + hakemus.oid, Serialization.write(hakemus.toHakemusMuutos)) {
      f
    }
  }

  def setupFixture(fixtureName: String)(implicit appConfig: AppConfig) = {
    new FixtureImporter(dao, springContext.mongoTemplate).applyFixtures(fixtureName)
  }

  def setApplicationStart(applicationId: String, daysFromNow: Long)(implicit personOid: PersonOid) = {
    withApplications { applications =>
      val hakuOid = applications.find(_.oid == applicationId).map(_.haku.oid).get
      put("util/fixtures/haku/" + hakuOid + "/overrideStart/" + (new Date().getTime + daysFromNow*24*60*60*1000), Iterable.empty) { }
    }
  }

  def modifyHakemus[T](oid: String)(modification: (Hakemus => Hakemus))(f: Hakemus => T)(implicit personOid: PersonOid): T = {
    withHakemus(oid) { hakemus =>
      val modified = modification(hakemus)
      saveHakemus(modified) {
        f(modified)
      }
    }
  }

  def answerExtraQuestion(phaseId: String, questionId: String, answer: String)(hakemus: Hakemus) = {
    val answerToExtraQuestion: Answers = Map(phaseId -> Map(questionId -> answer))
    hakemus.copy(answers = hakemus.answers ++ answerToExtraQuestion)
  }

  def removeHakutoive(hakemus: Hakemus) = {
    hakemus.copy(hakutoiveet = hakemus.hakutoiveet.slice(0, 2))
  }

  def addHakutoive(hakutoive: Hakutoive)(hakemus: Hakemus) = {
    val emptyIndex = hakemus.hakutoiveet.indexWhere(_.hakemusData.isEmpty)
    hakemus.copy(hakutoiveet = hakemus.hakutoiveet.patch(emptyIndex, List(hakutoive), 1))
  }

  def withSavedApplication[T](hakemus: Hakemus)(f: Application => T): T = {
    val application = dao.find(new Application().setOid(hakemus.oid)).get(0)
    f(application)
  }

  def hasSameHakuToiveet(hakemus1: Hakemus, hakemus2: Hakemus) = {
    hakemus1.hakutoiveet.equals(hakemus2.hakutoiveet)
  }
}

class HakemuksenTilaSerializer extends Serializer[HakemuksenTila] {
  private val TilaClass = classOf[HakemuksenTila]

  // Note: this serializer is for tests only, as you can see from the implementation below!

  override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), HakemuksenTila] = {
    case (TypeInfo(TilaClass, _), JObject(fields: List[JField])) =>
      Active()
  }

  override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case any: HakemuksenTila => Extraction.decompose(any)(JsonFormats.genericFormats)
  }
}

trait FixturePerson {
  implicit val personOid: PersonOid = PersonOid(TestFixture.personOid)
}
