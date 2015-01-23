package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.omatsivut.domain.Language.Language

case class Muistilista(otsikko: String, kieli: Language, vastaanottaja: List[String], koids: List[String], captcha: String)

case class KoulutusInformaatioBasketItem(applicationSystemName: String, applicationOptions: List[MuistilistaKoulutusInfo])
case class MuistilistaKoulutusInfo(name: String, providerName: String)