package fi.vm.sade.omatsivut.domain

case class Hakemus(oid: String, received: Long, var hakutoiveet: List[Map[String, String]] = Nil, var haku: Option[Haku] = None)
