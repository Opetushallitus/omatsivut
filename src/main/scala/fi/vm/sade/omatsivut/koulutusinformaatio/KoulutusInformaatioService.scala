package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.koulutusinformaatio.domain.{Koulutus, Opetuspiste}
import fi.vm.sade.omatsivut.domain.{Language, Address, Attachment}

trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String, lang: String): Option[List[Opetuspiste]]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, lang: String): Option[List[Koulutus]]
  def koulutus(aoId: String, lang: String): Option[Koulutus]
}
