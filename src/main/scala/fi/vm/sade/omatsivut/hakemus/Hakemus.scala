package fi.vm.sade.omatsivut.hakemus

case class Hakemus(oid: String, received: Long, var hakutoiveet: List[Map[String, String]] = Nil, var haku: Option[Haku] = None)
