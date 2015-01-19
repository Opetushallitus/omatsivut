package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.omatsivut.domain.Language.Language

case class Muistilista(lahettaja: Option[String], otsikko: String, kieli: Language, vastaaanottaja: List[String], koids: List[String])

case class KoulutusInformaatioBasketItem(applicationSystemName: String, applicationOptions: List[MuistilistaKoulutusInfo])
case class MuistilistaKoulutusInfo(name: String, providerName: String)