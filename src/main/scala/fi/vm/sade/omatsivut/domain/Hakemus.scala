package fi.vm.sade.omatsivut.domain

import fi.vm.sade.omatsivut.domain.Hakemus._

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
                  ) extends HakemuksenTunniste

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
case class Passive(id: String = "PASSIVE") extends HakemuksenTila // Passiivinen/poistettu
case class Incomplete(id: String = "INCOMPLETE") extends HakemuksenTila // Tietoja puuttuu

case class ValintaTulos()