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
                    postProcessingState: String,
                    hakutoiveet: List[Hakutoive] = Nil,
                    haku: Option[Haku] = None,
                    educationBackground: EducationBackground,
                    answers: Answers
                  )

case class EducationBackground(baseEducation: String, vocational: Boolean)