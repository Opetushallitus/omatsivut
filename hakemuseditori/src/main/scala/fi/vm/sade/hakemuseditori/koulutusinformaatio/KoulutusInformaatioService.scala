package fi.vm.sade.hakemuseditori.koulutusinformaatio

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.koulutusinformaatio.domain.{Koulutus, Opetuspiste}

trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String, lang: Language): Option[List[Opetuspiste]]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, lang: Language): Option[List[Koulutus]]
  def koulutus(aoId: String, lang: Language): Option[Koulutus]
  def opetuspiste(id: String, lang: Language): Option[Opetuspiste]
  def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]]
}
