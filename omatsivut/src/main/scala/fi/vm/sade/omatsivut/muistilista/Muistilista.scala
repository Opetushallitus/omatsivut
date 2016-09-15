package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.hakemuseditori.domain.Language.Language

case class Muistilista(otsikko: String, kieli: Language, vastaanottaja: List[String], koids: List[String], captcha: String)