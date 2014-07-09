package fi.vm.sade.omatsivut.domain

case class Hakemus(
                    oid: String,
                    received: Long,
                    hakutoiveet: List[Map[String, String]] = Nil,
                    haku: Option[Haku] = None,
                    baseEducation: String
                  )
