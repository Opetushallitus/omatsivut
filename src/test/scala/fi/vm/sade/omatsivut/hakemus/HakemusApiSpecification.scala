package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.hakemuseditori.fixtures.JsonFixtureMaps
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, HakemuksenTila, Hakemus}
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application
import fi.vm.sade.hakemuseditori.hakemus.{ApplicationsResponse, HakemusInfo}
import fi.vm.sade.hakemuseditori.json.{ApplicationSerializer, JsonFormats}
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.fixtures.hakemus.ApplicationFixtureImporter
import fi.vm.sade.omatsivut.{PersonOid, ScalatraTestSupport}
import fi.vm.sade.utils.json4s.GenericJsonFormats
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonAST.JObject
import org.json4s._
import org.json4s.jackson.Serialization
import org.json4s.reflect.TypeInfo

import java.util.Date

trait HakemusApiSpecification extends ScalatraTestSupport with Logging {
  implicit val jsonFormats: Formats = JsonFormats.jsonFormats ++ List(new HakemuksenTilaSerializer)

  lazy val fixtureImporter: ApplicationFixtureImporter = new ApplicationFixtureImporter(springContext)

  val henkilotiedot: String = "henkilotiedot"
  val hakutoiveet: String = "hakutoiveet"
  val osaaminen: String = "osaaminen"
  val henkilotunnus: String = "Henkilotunnus"
  val lahiosoite: String = "lahiosoite"

  def withHakemusWithEmptyAnswers[T](oid: String)(f: (HakemusInfo => T))(implicit personOid: PersonOid): T = {
    withApplicationsResponse { resp =>
      val originalHakemusInfo = resp.applications.find(_.hakemus.oid == oid).get
      f(originalHakemusInfo.copy(hakemus = originalHakemusInfo.hakemus.copy(answers = Hakemus.emptyAnswers)))
    }
  }

  def withHakemus[T](oid: String)(f: (HakemusInfo => T))(implicit personOid: PersonOid): T = {
    withApplicationsResponse { resp =>
      f(resp.applications.find(_.hakemus.oid == oid).get)
    }
  }

  def withApplicationsResponse[T](f: (ApplicationsResponse => T))(implicit personOid: PersonOid): T = {
    authGet("secure/applications") {
      val b = body
      val resp: ApplicationsResponse = Serialization.read[ApplicationsResponse](b)
      f(resp)
    }

  }

  def setupFixture(fixtureName: String) = {
    new ApplicationFixtureImporter(springContext).applyFixtures(fixtureName)
  }

  def setApplicationStart(applicationId: String, daysFromNow: Long)(implicit personOid: PersonOid) = {
    withApplicationsResponse { resp =>
      val hakuOid = resp.applications.find(_.hakemus.oid == applicationId).map(_.hakemus.haku.oid).get
      put("util/fixtures/haku/" + hakuOid + "/overrideStart/" + (new Date().getTime + daysFromNow*24*60*60*1000), Iterable.empty) { }
    }
  }

  def setHakukierrosPaattyy(hakuOid: String, daysFromNow: Long)(implicit personOid: PersonOid) = {
      put("util/fixtures/haku/" + hakuOid + "/overrideHakuKierrosPaattyy/" + (new Date().getTime + daysFromNow*24*60*60*1000), Iterable.empty) { }
  }

  def resetHakukierrosPaattyy(applicationId: String)(implicit personOid: PersonOid) = {
    withApplicationsResponse { resp =>
      val hakuOid = resp.applications.find(_.hakemus.oid == applicationId).map(_.hakemus.haku.oid).get
      put("util/fixtures/haku/" + hakuOid + "/resetHakuPaattyy") {}
    }
  }

  def withSavedApplication[T](hakemus: Hakemus)(f: Application => T): T = withSavedApplication(hakemus.oid)(f)

  def withSavedApplication[T](hakemusOid: String)(f: Application => T): T = {
    val application = JsonFixtureMaps.findByKey[Application]("/hakemuseditorimockdata/applications-hakuapp.json", hakemusOid).get
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
