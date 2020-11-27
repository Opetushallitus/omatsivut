package fi.vm.sade.hakemuseditori.hakemus.domain

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, KohteenHakuaika, KoulutuksenAlkaminen}
import fi.vm.sade.omatsivut.OphUrlProperties
import org.json4s.{DefaultFormats, JArray, JField, JString, JValue}

object Hakemus {
  type Valintatulos = JValue
  type Answers = Map[String, Map[String, String]]
  type HakutoiveData = Map[String, String]
  val emptyAnswers = Map.empty.asInstanceOf[Map[String, Map[String, String]]]

  def valintatulosHasSomeResults(valintatulos: Option[Valintatulos]): Boolean = {
    implicit val formats = DefaultFormats
    val hakutoiveet: List[JValue] = valintatulos.map(_ \ "hakutoiveet").map(_.children).getOrElse(List.empty)
    hakutoiveet.exists(hakutoive => (hakutoive \ "valintatila").extract[String] != "KESKEN")
  }

  def valintatulosWithoutKelaUrl(v: Valintatulos): Valintatulos = {
    v.transformField {
      case ("hakutoiveet", a:JArray) => ("hakutoiveet", JArray(a.arr.map(ht => {
        // removes kela URL
        ht.removeField {
          case JField("kelaURL", i: JString) => true
          case _ => false
        }
      })))
    }
  }
}

case class Hakemus(oid: String,
                   personOid: String,
                   received: Option[Long],
                   updated: Option[Long],
                   state: HakemuksenTila,
                   tuloskirje: Option[Tuloskirje] = None,
                   ohjeetUudelleOpiskelijalle: Map[String, String], // hakukohdeOid -> linkki
                   hakutoiveet: List[Hakutoive] = Nil,
                   haku: Haku,
                   educationBackground: EducationBackground,
                   answers: Answers,
                   postOffice: Option[String],
                   email: Option[String],
                   requiresAdditionalInfo: Boolean,
                   hasForm: Boolean,
                   requiredPaymentState: Option[String],
                   notifications: Map[String, Map[String, Boolean]]) extends HakemusLike {
  def preferences = hakutoiveet.map(_.hakemusData.getOrElse(Map.empty))

  def toHakemusMuutos = HakemusMuutos(oid, haku.oid, hakutoiveet.map(_.hakemusData.getOrElse(Map.empty)), answers)

  def withoutKelaUrl: Hakemus = copy(state = state.withoutKelaUrl)

  def omatsivutPreviewUrl: Option[String] = if (hasForm) {
    Some(OphUrlProperties.url("omatsivut.applications.preview", oid))
  } else {
    None
  }
}

case class Tuloskirje(hakuOid: String, created: Long)

case class Hakutoive(hakemusData: Option[HakutoiveData],
                     koulutuksenAlkaminen: Option[KoulutuksenAlkaminen],
                     hakuaikaId: Option[String],
                     hakukohdekohtaisetHakuajat: Option[List[KohteenHakuaika]])

case class HakemusMuutos(oid: String, hakuOid: String, hakutoiveet: List[HakutoiveData] = Nil, answers: Answers) extends HakemusLike {
  def preferences = hakutoiveet
}

trait HakemusLike {
  def oid: String

  def preferences: List[HakutoiveData]

  def answers: Answers
}

case class EducationBackground(baseEducation: String, vocational: Boolean)

sealed trait HakemuksenTila {
  val id: String
  def withoutKelaUrl: HakemuksenTila
}

// Alkutila, ei editoitavissa
case class Submitted(id: String = "SUBMITTED") extends HakemuksenTila {
  override def withoutKelaUrl: HakemuksenTila = this
}

// Taustaprosessointi kesken, ei editoitavissa
case class PostProcessing(id: String = "POSTPROCESSING") extends HakemuksenTila {
  override def withoutKelaUrl: HakemuksenTila = this
}

// Aktiivinen, editoitavissa
case class Active(id: String = "ACTIVE", valintatulos: Option[Valintatulos] = None) extends HakemuksenTila {
  override def withoutKelaUrl: HakemuksenTila = Active(id, valintatulos.map(Hakemus.valintatulosWithoutKelaUrl))
}

// Hakukausi päättynyt
case class HakukausiPaattynyt(id: String = "HAKUKAUSIPAATTYNYT", valintatulos: Option[Valintatulos] = None) extends HakemuksenTila {
  override def withoutKelaUrl: HakemuksenTila = HakukausiPaattynyt(id, valintatulos.map(Hakemus.valintatulosWithoutKelaUrl))
}

// Hakukierros päättynyt
case class HakukierrosPaattynyt(id: String = "HAKUKIERROSPAATTYNYT", valintatulos: Option[Valintatulos] = None) extends HakemuksenTila {
  override def withoutKelaUrl: HakemuksenTila = HakukierrosPaattynyt(id, valintatulos.map(Hakemus.valintatulosWithoutKelaUrl))
}

// Passiivinen/poistettu
case class Passive(id: String = "PASSIVE") extends HakemuksenTila {
  override def withoutKelaUrl: HakemuksenTila = this
}

// Tietoja puuttuu
case class Incomplete(id: String = "INCOMPLETE") extends HakemuksenTila {
  override def withoutKelaUrl: HakemuksenTila = this
}
