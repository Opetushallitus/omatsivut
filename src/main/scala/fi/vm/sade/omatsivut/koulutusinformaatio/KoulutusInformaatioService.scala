package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.koulutusinformaatio.domain.{Koulutus, Opetuspiste}
import fi.vm.sade.omatsivut.domain.{Language, Address, Attachment}

trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String): Option[List[Opetuspiste]]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: String): Option[List[Koulutus]]
  def koulutus(aoId: String): Option[Koulutus]

  def liitepyynto(aoId: String, heading: String, description: String)(implicit lang: Language.Language): Attachment = {
    val liitepyynto = koulutus(aoId).map(koulutus => Attachment(
              heading,
              description,
              koulutus.provider map(_.name),
              getAttachmentRecipient(koulutus),
              getAttachmentAddress(koulutus),
              koulutus.attachmentDeliveryDeadline
        ))
    liitepyynto.getOrElse(Attachment(heading, description))
  }

  private def getAttachmentAddress(koulutus: Koulutus): Option[Address] = {
    if(koulutus.attachmentDeliveryAddress.isDefined) {
      koulutus.attachmentDeliveryAddress
    }
    else {
      koulutus.provider flatMap(_.applicationOffice) flatMap(_.postalAddress )
    }
  }

  private def getAttachmentRecipient(koulutus: Koulutus): Option[String] = {
    val applicationOfficeName = koulutus.provider flatMap(_.applicationOffice) flatMap(_.name)
    if(applicationOfficeName.isDefined) {
      applicationOfficeName
    }
    else {
       koulutus.provider map(_.name)
    }
  }

}
