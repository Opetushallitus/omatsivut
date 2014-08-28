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
                    state: String,
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

case class ValintaTulos()