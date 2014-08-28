package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.{Active, HakemuksenTila, Hakemus}
import fi.vm.sade.omatsivut.domain.Hakemus.{Hakutoive, Answers}
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.json.{QuestionNodeSerializer, JsonFormats}
import org.json4s.JsonAST.JObject
import org.json4s._
import org.json4s.jackson.Serialization
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import org.json4s.reflect.TypeInfo

trait HakemusApiSpecification extends ScalatraTestSupport {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats ++ List(new HakemuksenTilaSerializer)

  val personalInfoPhaseKey: String = OppijaConstants.PHASE_PERSONAL
  val preferencesPhaseKey: String = OppijaConstants.PHASE_APPLICATION_OPTIONS
  val skillsetPhaseKey: String = OppijaConstants.PHASE_GRADES
  val ssnKey: String = OppijaConstants.ELEMENT_ID_SOCIAL_SECURITY_NUMBER

  def withHakemus[T](oid: String)(f: (Hakemus => T)): T = {
    withApplications(oid) { applications =>
      val hakemus = applications.find(_.oid == oid).get.copy(answers = Hakemus.emptyAnswers)
      f(hakemus)
    }
  }

  def withApplications[T](oid: String)(f: (List[Hakemus] => T)): T = {
    authGet("/applications", personOid) {
      val applications: List[Hakemus] = Serialization.read[List[Hakemus]](body)
      f(applications)
    }
  }

  def saveHakemus[T](hakemus: Hakemus)(f: => T): T = {
    authPut("/applications/" + hakemus.oid, personOid, Serialization.write(hakemus.toHakemusMuutos)) {
      f
    }
  }

  def setupFixture(fixtureName: String)(implicit appConfig: AppConfig) = {
    FixtureImporter.apply().applyFixtures(fixtureName)
  }

  def modifyHakemus[T](oid: String)(modification: (Hakemus => Hakemus))(f: Hakemus => T): T = {
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
    val emptyIndex = hakemus.hakutoiveet.indexWhere(_.isEmpty)
    hakemus.copy(hakutoiveet = hakemus.hakutoiveet.patch(emptyIndex, List(hakutoive), 1))
  }

  def withSavedApplication[T](hakemus: Hakemus)(f: Application => T): T = {
    val application = appConfig.springContext.applicationDAO.find(new Application().setOid(hakemus.oid)).get(0)
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