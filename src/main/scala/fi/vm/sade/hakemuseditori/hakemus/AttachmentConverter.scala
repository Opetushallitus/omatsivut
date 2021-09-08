package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.ApplicationAttachment
import fi.vm.sade.hakemuseditori.domain.{Address, Attachment, Language}

object AttachmentConverter {
  def convertToAttachment(attachment: ApplicationAttachment)(implicit language: Language.Language): Attachment = {
    val address = Option(attachment.getAddress())
    Attachment(
            Option(attachment.getName()).map(_.getText(language.toString())),
            Option(attachment.getHeader()).map(_.getText(language.toString())),
            Option(attachment.getDescription()).map(_.getText(language.toString())),
            address.map(x => Option(x.getRecipient())).flatten,
            address.map(convertToAddress(_)),
            Option(attachment.getDeadline()).map(_.getTime()),
            Option(attachment.getEmailAddress)
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