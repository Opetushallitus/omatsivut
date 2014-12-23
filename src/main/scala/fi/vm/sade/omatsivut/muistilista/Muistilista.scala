package fi.vm.sade.omatsivut.muistilista

case class Muistilista(lahettaja: Option[String], otsikko: String, kieli: String, vastaaanottaja: List[String], oids: List[String])
