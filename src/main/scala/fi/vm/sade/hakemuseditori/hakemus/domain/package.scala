package fi.vm.sade.hakemuseditori.hakemus.domain

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, KohteenHakuaika}
import org.json4s.{DefaultFormats, JValue}

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
}

case class Hakemus(oid: String,
                   personOid: String,
                   received: Option[Long],
                   updated: Option[Long],
                   state: HakemuksenTila,
                   tuloskirje: Option[Tuloskirje] = None,
                   hakutoiveet: List[Hakutoive] = Nil,
                   haku: Haku,
                   educationBackground: EducationBackground,
                   answers: Answers,
                   postOffice: Option[String],
                   email: Option[String],
                   requiresAdditionalInfo: Boolean,
                   hasForm: Boolean,
                   requiredPaymentState: Option[String],
                   notifications: Map[String, Map[String, Boolean]],
                   secret: Option[String] = None) extends HakemusLike {
  def preferences = hakutoiveet.map(_.hakemusData.getOrElse(Map.empty))

  def toHakemusMuutos = HakemusMuutos(oid, haku.oid, hakutoiveet.map(_.hakemusData.getOrElse(Map.empty)), answers)
}

case class Tuloskirje(hakuOid: String, created: Long)

case class Hakutoive(hakemusData: Option[HakutoiveData], hakuaikaId: Option[String] = None, kohdekohtainenHakuaika: Option[KohteenHakuaika] = None)

object Hakutoive {
  def empty = Hakutoive(None)
}

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
}

// Alkutila, ei editoitatissa
case class Submitted(id: String = "SUBMITTED") extends HakemuksenTila

// Taustaprosessointi kesken, ei editoitavissa
case class PostProcessing(id: String = "POSTPROCESSING") extends HakemuksenTila

// Aktiivinen, editoitavissa
case class Active(id: String = "ACTIVE", valintatulos: Option[Valintatulos] = None) extends HakemuksenTila

// Hakukausi päättynyt
case class HakukausiPaattynyt(id: String = "HAKUKAUSIPAATTYNYT", valintatulos: Option[Valintatulos] = None) extends HakemuksenTila

// Hakukierros päättynyt
case class HakukierrosPaattynyt(id: String = "HAKUKIERROSPAATTYNYT", valintatulos: Option[Valintatulos] = None) extends HakemuksenTila

// Passiivinen/poistettu
case class Passive(id: String = "PASSIVE") extends HakemuksenTila

// Tietoja puuttuu
case class Incomplete(id: String = "INCOMPLETE") extends HakemuksenTila
