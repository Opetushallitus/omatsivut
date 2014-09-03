package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.koulutusinformaatio.domain.{Liitepyynto, Koulutus, Opetuspiste}
import fi.vm.sade.omatsivut.domain.{Language, Address}

trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String): Option[List[Opetuspiste]]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: String): Option[List[Koulutus]]
  def koulutus(aoId: String): Option[Koulutus]

  def liitepyynto(aoId: String)(implicit lang: Language.Language): Liitepyynto = {
    val liitepyynto = koulutus(aoId).map(koulutus => Liitepyynto(
              aoId,
              koulutus.provider.map(_.name),
              getAttachmentAddress(koulutus),
              koulutus.attachmentDeliveryDeadline
        ))
    liitepyynto.getOrElse(Liitepyynto(aoId))
  }

  private def getAttachmentAddress(info: Koulutus): Option[Address] = {
    if(info.attachmentDeliveryAddress.isDefined) {
      info.attachmentDeliveryAddress
    }
    else {
      info.provider flatMap(_.applicationOffice) flatMap(_.postalAddress )
    }
  }
}
