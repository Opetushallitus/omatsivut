package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.koulutusinformaatio.domain.{Koulutus, Opetuspiste}

trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String, lang: Language): Option[List[Opetuspiste]]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, lang: Language): Option[List[Koulutus]]
  def koulutus(aoId: String, lang: Language): Option[Koulutus]
  def opetuspiste(id: String, lang: Language): Option[Opetuspiste]
}
