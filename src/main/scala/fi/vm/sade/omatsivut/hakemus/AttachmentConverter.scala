package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.localization.Translations
import fi.vm.sade.omatsivut.domain.Attachment
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import scala.collection.JavaConversions._
import fi.vm.sade.haku.oppija.hakemus.domain.util.ApplicationUtil
import fi.vm.sade.haku.oppija.hakemus.domain.util.AttachmentUtil
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.koulutusinformaatio.impl.KoulutusinformaatioServiceImpl
import fi.vm.sade.haku.oppija.hakemus.domain.dto.ApplicationAttachment
import fi.vm.sade.omatsivut.domain.Attachment
import fi.vm.sade.omatsivut.domain.Address
import fi.vm.sade.omatsivut.servlet.ServerContaxtPath

object AttachmentConverter {

  def getAttachments(serverPath: ServerContaxtPath, appSystem: ApplicationSystem, application: Application)(implicit language: Language.Language): List[Attachment] = {
    val attachmentInfo = AttachmentUtil.resolveAttachments(appSystem, application, getKoulutusinformattioService(serverPath), language.toString())
    attachmentInfo.toList.map(convertToAttachment(_))
  }

  def convertToAttachment(attachment: ApplicationAttachment)(implicit language: Language.Language): Attachment = {
    val address = Option(attachment.getAddress())
    Attachment(
            Option(attachment.getName()).flatMap(_.getTranslations().toMap.get(language.toString())),
            Option(attachment.getHeader()).flatMap(_.getTranslations().toMap.get(language.toString())),
            Option(attachment.getDescription()).flatMap(_.getTranslations().toMap.get(language.toString())),
            address.map(x => Option(x.getRecipient())).flatten,
            address.map(convertToAddress(_)),
            Option(attachment.getDeadline()).map(_.getTime())
          )
  }

  private def convertToAddress(address: fi.vm.sade.haku.oppija.hakemus.domain.dto.Address): Address = {
    Address(
         Option(address.getStreetAddress()),
         Option(address.getStreetAddress2()),
         Option(address.getPostalCode()),
         Option(address.getPostOffice())
       )
  }

  private def getKoulutusinformattioService(serverPath: ServerContaxtPath) = {
    val koulutusInformaatio = new KoulutusinformaatioServiceImpl()
    koulutusInformaatio.setTargetService(serverPath.path + "/koulutusinformaatio/koulutus")
    koulutusInformaatio
  }

}