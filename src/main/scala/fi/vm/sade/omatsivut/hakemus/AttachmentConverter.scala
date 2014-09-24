package fi.vm.sade.omatsivut.hakemus

import scala.collection.JavaConversions._

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.domain.ApplicationAttachment
import fi.vm.sade.haku.oppija.hakemus.domain.util.AttachmentUtil
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.{Address, Attachment, Language}
import fi.vm.sade.omatsivut.haku.ElementWrapper

object AttachmentConverter {

  def getAttachments(application: Application)(implicit language: Language.Language): List[Attachment] = {
    val attachmentInfo = AttachmentUtil.resolveAttachments(application)
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

  def requiresAdditionalInfo(applicationSystem: ApplicationSystem, application: Application): Boolean = {
    !AttachmentUtil.resolveAttachments(application).isEmpty() ||
    !(for(addInfo <- applicationSystem.getAdditionalInformationElements())
      yield ElementWrapper.wrapFiltered(addInfo, application.getVastauksetMerged().toMap)
    ).filterNot(_.children.isEmpty).isEmpty
  }


  private def convertToAddress(address: fi.vm.sade.haku.oppija.hakemus.domain.Address): Address = {
    Address(
         Option(address.getStreetAddress()),
         Option(address.getStreetAddress2()),
         Option(address.getPostalCode()),
         Option(address.getPostOffice())
       )
  }

}