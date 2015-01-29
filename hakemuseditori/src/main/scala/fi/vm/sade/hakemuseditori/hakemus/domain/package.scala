package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, KohteenHakuaika}
import fi.vm.sade.hakemuseditori.tulokset.HakemuksenValintatulos

package object domain {
  object Hakemus {
    type Answers = Map[String, Map[String, String]]
    type HakutoiveData = Map[String, String]

    val emptyAnswers = Map.empty.asInstanceOf[Map[String, Map[String, String]]]
  }
  case class Hakemus(
                      oid: String,
                      received: Option[Long],
                      updated: Option[Long],
                      state: HakemuksenTila,
                      hakutoiveet: List[Hakutoive] = Nil,
                      haku: Haku,
                      educationBackground: EducationBackground,
                      answers: Answers,
                      postOffice: Option[String],
                      requiresAdditionalInfo: Boolean,
                      hasForm: Boolean
                      ) extends HakemusLike {
    def preferences = hakutoiveet.map(_.hakemusData.getOrElse(Map.empty))
    def toHakemusMuutos = HakemusMuutos(oid, haku.oid, hakutoiveet.map(_.hakemusData.getOrElse(Map.empty)), answers)
  }

  case class Hakutoive(hakemusData: Option[HakutoiveData], hakuaikaId: Option[String] = None, kohdekohtainenHakuaika: Option[KohteenHakuaika] = None)
  
  object Hakutoive {
    def empty = Hakutoive(None)
  }

  case class HakemusMuutos (
                             oid: String,
                             hakuOid: String,
                             hakutoiveet: List[HakutoiveData] = Nil,
                             answers: Answers
                             ) extends HakemusLike {
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

  case class Submitted(id: String = "SUBMITTED") extends HakemuksenTila // Alkutila, ei editoitatissa
  case class PostProcessing(id: String = "POSTPROCESSING") extends HakemuksenTila // Taustaprosessointi kesken, ei editoitavissa
  case class Active(id: String = "ACTIVE", valintatulos: Option[HakemuksenValintatulos] = None) extends HakemuksenTila // Aktiivinen, editoitavissa
  case class HakukausiPaattynyt(id: String = "HAKUKAUSIPAATTYNYT", valintatulos: Option[HakemuksenValintatulos] = None) extends HakemuksenTila // Hakukausi päättynyt
  case class HakukierrosPaattynyt(id: String = "HAKUKIERROSPAATTYNYT", valintatulos: Option[HakemuksenValintatulos] = None) extends HakemuksenTila // Hakukierros päättynyt
  case class Passive(id: String = "PASSIVE") extends HakemuksenTila // Passiivinen/poistettu
  case class Incomplete(id: String = "INCOMPLETE") extends HakemuksenTila // Tietoja puuttuu

}
