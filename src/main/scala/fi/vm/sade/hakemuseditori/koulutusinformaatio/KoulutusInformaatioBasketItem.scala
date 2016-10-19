package fi.vm.sade.hakemuseditori.koulutusinformaatio

case class KoulutusInformaatioBasketItem(applicationSystemId: String, applicationSystemName: String, applicationOptions: List[MuistilistaKoulutusInfo])

case class MuistilistaKoulutusInfo(name: String, providerName: String, id: String)