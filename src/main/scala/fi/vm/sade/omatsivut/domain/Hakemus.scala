package fi.vm.sade.omatsivut.domain

import fi.vm.sade.omatsivut.domain.Hakemus.Answers

object Hakemus {
  type Answers = Map[String, Map[String, String]]
  val emptyAnswers = Map.empty.asInstanceOf[Map[String, Map[String, String]]]
}
case class Hakemus(
                    oid: String,
                    received: Long,
                    hakutoiveet: List[Map[String, String]] = Nil,
                    haku: Option[Haku] = None,
                    educationBackground: EducationBackground,
                    answers: Answers = Map.empty
                  )

case class EducationBackground(baseEducation: String, vocational: Boolean)