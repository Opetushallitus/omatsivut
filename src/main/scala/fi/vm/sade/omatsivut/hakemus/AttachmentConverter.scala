package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.ApplicationAttachment
import fi.vm.sade.omatsivut.domain.{Address, Attachment, Language}

import scala.collection.JavaConversions._

object AttachmentConverter {
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

  private def convertToAddress(address: fi.vm.sade.haku.oppija.hakemus.domain.Address): Address = {
    Address(
         Option(address.getStreetAddress()),
         Option(address.getStreetAddress2()),
         Option(address.getPostalCode()),
         Option(address.getPostOffice())
       )
  }
}