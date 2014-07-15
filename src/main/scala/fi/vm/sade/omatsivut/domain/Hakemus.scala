package fi.vm.sade.omatsivut.domain

case class Hakemus(
                    oid: String,
                    received: Long,
                    hakutoiveet: List[Map[String, String]] = Nil,
                    haku: Option[Haku] = None,
                    educationBackground: EducationBackground,
                    answers: Map[String, Map[String, String]] = Map.empty
                  )

case class EducationBackground(baseEducation: String, vocational: Boolean)