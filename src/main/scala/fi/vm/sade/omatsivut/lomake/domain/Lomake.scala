package fi.vm.sade.omatsivut.lomake.domain

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.domain.util.AttachmentUtil
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Form, Element}
import fi.vm.sade.omatsivut.lomake.ElementWrapper
import scala.collection.JavaConversions._

case class Lomake(oid: String, form: Form, private val additionalInformation: List[Element]) {
  def requiresAdditionalInfo(application: Application): Boolean = {
    !AttachmentUtil.resolveAttachments(application).isEmpty() ||
      !(for(addInfo <- additionalInformation)
      yield ElementWrapper.wrapFiltered(addInfo, application.getVastauksetMerged().toMap)
        ).filterNot(_.children.isEmpty).isEmpty
  }
}

object Lomake {
  def apply(applicationSystem: ApplicationSystem): Lomake = {
    new Lomake(applicationSystem.getId, applicationSystem.getForm, applicationSystem.getAdditionalInformationElements.toList)
  }
}
