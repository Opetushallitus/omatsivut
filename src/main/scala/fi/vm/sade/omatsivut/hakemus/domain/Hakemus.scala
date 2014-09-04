package fi.vm.sade.omatsivut.hakemus.domain

import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

object Hakemus {
  type Answers = Map[String, Map[String, String]]
  type Hakutoive = Map[String, String]

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
  def toHakemusMuutos = HakemusMuutos(oid, haku.oid, hakutoiveet, answers)
}

case class HakemusMuutos (
                    oid: String,
                    hakuOid: String,
                    hakutoiveet: List[Hakutoive] = Nil,
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

case class Valintatulos(hakutoiveet: List[HakutoiveenValintatulos])
case class HakutoiveenValintatulos(
                                    koulutus: Koulutus,
                                    opetuspiste: Opetuspiste,
                                    tila: String,
                                    vastaanottotieto: String,
                                    ilmoittautumisTila: String,
                                    jonosija: Option[Int],
                                    varasijaNumero: Option[Int])

case class Koulutus(oid: String, name: String)
case class Opetuspiste(oid: String, name: String)
