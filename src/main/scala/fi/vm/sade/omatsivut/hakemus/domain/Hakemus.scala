package fi.vm.sade.omatsivut.hakemus.domain

import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain.ResultState.ResultState
import fi.vm.sade.omatsivut.hakemus.domain.HakutoiveenValintatulosTila.HakutoiveenValintatulosTila
import fi.vm.sade.omatsivut.hakemus.domain.VastaanotettavuusTila.VastaanotettavuusTila
import fi.vm.sade.omatsivut.tarjonta.{KohteenHakuaika, Haku}

object Hakemus {
  type Answers = Map[String, Map[String, String]]
  type HakutoiveData = Map[String, String]

  val emptyAnswers = Map.empty.asInstanceOf[Map[String, Map[String, String]]]
}
case class Hakemus(
                    oid: String,
                    received: Long,
                    updated: Long,
                    state: HakemuksenTila,
                    hakutoiveet: List[Hakutoive] = Nil,
                    haku: Haku,
                    educationBackground: EducationBackground,
                    answers: Answers,
                    requiresAdditionalInfo: Boolean
                  ) extends HakemuksenTunniste {

  def toHakemusMuutos = HakemusMuutos(oid, haku.oid, hakutoiveet.map(_.hakemusData.getOrElse(Map.empty)), answers)
}

case class Hakutoive(val hakemusData: Option[HakutoiveData], hakuaikaId: Option[String] = None, kohdekohtainenHakuaika: Option[KohteenHakuaika] = None)
object Hakutoive {
  def empty = Hakutoive(None)
}

case class HakemusMuutos (
                    oid: String,
                    hakuOid: String,
                    hakutoiveet: List[HakutoiveData] = Nil,
                    answers: Answers
                    ) extends HakemuksenTunniste

trait HakemuksenTunniste {
  def oid: String
}

case class EducationBackground(baseEducation: String, vocational: Boolean)

sealed trait HakemuksenTila {
  val id: String
}

case class Submitted(id: String = "SUBMITTED") extends HakemuksenTila // Alkutila, ei editoitatissa
case class PostProcessing(id: String = "POSTPROCESSING") extends HakemuksenTila // Taustaprosessointi kesken, ei editoitavissa
case class Active(id: String = "ACTIVE") extends HakemuksenTila // Aktiivinen, editoitavissa
case class HakuPaattynyt(id: String = "HAKUPAATTYNYT", valintatulos: Option[Valintatulos] = None) extends HakemuksenTila // Haku päättynyt
case class Passive(id: String = "PASSIVE") extends HakemuksenTila // Passiivinen/poistettu
case class Incomplete(id: String = "INCOMPLETE") extends HakemuksenTila // Tietoja puuttuu

object ResultState extends Enumeration {
  type ResultState = Value
  val VASTAANOTTANUT, EI_VASTAANOTETTU_MAARA_AIKANA, EHDOLLISESTI_VASTAANOTTANUT, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN = Value
}

case class Valintatulos(hakutoiveet: List[HakutoiveenValintatulos])
case class HakutoiveenValintatulos(
                                    koulutus: Koulutus,
                                    opetuspiste: Opetuspiste,
                                    tila: HakutoiveenValintatulosTila,
                                    vastaanottotila: ResultState,
                                    vastaanotettavuustila: VastaanotettavuusTila,
                                    vastaanotettavissaAsti: Option[Long],
                                    viimeisinValintatuloksenMuutos: Option[Long],
                                    ilmoittautumistila: Option[String],
                                    jonosija: Option[Int],
                                    varasijojaTaytetaanAsti: Option[Long],
                                    varasijanumero: Option[Int],
                                    tilankuvaus: Option[String]) {
  def isPeruuntunut = {
    vastaanottotila == ResultState.PERUNUT || vastaanottotila == ResultState.PERUUTETTU || vastaanottotila == ResultState.PERUUNTUNUT
  }
}

case class Koulutus(oid: String, name: String)
case class Opetuspiste(oid: String, name: String)

object HakutoiveenValintatulosTila extends Enumeration {
  type HakutoiveenValintatulosTila = Value
  val HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARALLA, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN = Value
}

object VastaanotettavuusTila extends Enumeration {
  type VastaanotettavuusTila = Value
  val EI_VASTAANOTETTAVISSA, VASTAANOTETTAVISSA_SITOVASTI, VASTAANOTETTAVISSA_EHDOLLISESTI = Value
}
