package fi.vm.sade.omatsivut.hakemus

import java.util.Date
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.hakemus.{HakemusSpringContext, HakemusInfo}
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, Hakutoive, HakemuksenTila, Hakemus}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.hakemus.ApplicationFixtureImporter
import fi.vm.sade.omatsivut.{SharedAppConfig, PersonOid, ScalatraTestSupport}
import fi.vm.sade.utils.json4s.GenericJsonFormats
import org.json4s.JsonAST.JObject
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.reflect.TypeInfo

trait HakemusApiSpecification extends ScalatraTestSupport {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats ++ List(new HakemuksenTilaSerializer)

  private lazy val springContext: HakemusSpringContext = SharedAppConfig.componentRegistry.springContext
  private lazy val dao: ApplicationDAO = springContext.applicationDAO

  lazy val fixtureImporter: ApplicationFixtureImporter = new ApplicationFixtureImporter(springContext)

  val henkilotiedot: String = "henkilotiedot"
  val hakutoiveet: String = "hakutoiveet"
  val osaaminen: String = "osaaminen"
  val henkilotunnus: String = "Henkilotunnus"
  val lahiosoite: String = "lahiosoite"

  def withHakemus[T](oid: String)(f: (HakemusInfo => T))(implicit personOid: PersonOid): T = {
    withApplications { applications =>
      val originalHakemusInfo = applications.find(_.hakemus.oid == oid).get
      f(originalHakemusInfo.copy(hakemus = originalHakemusInfo.hakemus.copy(answers = Hakemus.emptyAnswers)))
    }
  }

  def withApplications[T](f: (List[HakemusInfo] => T))(implicit personOid: PersonOid): T = {
    authGet("secure/applications") {
      val b = body
      val applications: List[HakemusInfo] = Serialization.read[List[HakemusInfo]](b)
      f(applications)
    }
  }

  def saveHakemus[T](hakemus: Hakemus)(f: => T)(implicit personOid: PersonOid): T = {
    authPut("secure/applications/" + hakemus.oid, Serialization.write(hakemus.toHakemusMuutos)) {
      f
    }
  }

  def setupFixture(fixtureName: String) = {
    new ApplicationFixtureImporter(springContext).applyFixtures(fixtureName)
  }

  def setApplicationStart(applicationId: String, daysFromNow: Long)(implicit personOid: PersonOid) = {
    withApplications { applications =>
      val hakuOid = applications.find(_.hakemus.oid == applicationId).map(_.hakemus.haku.oid).get
      put("util/fixtures/haku/" + hakuOid + "/overrideStart/" + (new Date().getTime + daysFromNow*24*60*60*1000), Iterable.empty) { }
    }
  }

  def modifyHakemus[T](oid: String)(modification: (Hakemus => Hakemus))(f: Hakemus => T)(implicit personOid: PersonOid): T = {
    withHakemus(oid) { hakemus =>
      val modified = modification(hakemus.hakemus)
      saveHakemus(modified) {
        f(modified)
      }
    }
  }

  def answerExtraQuestion(phaseId: String, questionId: String, answer: String)(hakemus: Hakemus) = {
    val answerToExtraQuestion: Answers = Map(phaseId -> (hakemus.answers.getOrElse(phaseId, Map.empty) + (questionId -> answer)))
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
    case any: HakemuksenTila => Extraction.decompose(any)(GenericJsonFormats.genericFormats)
  }
}

trait FixturePerson {
  implicit val personOid: PersonOid = PersonOid(TestFixture.personOid)
}
