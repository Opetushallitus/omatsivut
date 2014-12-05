package fi.vm.sade.omatsivut.lomake.domain

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Element, Form}
import fi.vm.sade.omatsivut.hakemus.ImmutableLegacyApplicationWrapper
import fi.vm.sade.omatsivut.lomake.ElementWrapper

import scala.collection.JavaConversions._

case class Lomake(oid: String, form: Form, private val additionalInformation: List[Element]) {
  def requiresAdditionalInfo(application: ImmutableLegacyApplicationWrapper): Boolean = {
    !application.attachments.isEmpty ||
      !(for(addInfo <- additionalInformation)
      yield ElementWrapper.wrapFiltered(addInfo, application.flatAnswers)
        ).filterNot(_.children.isEmpty).isEmpty
  }
}

object Lomake {
  def apply(applicationSystem: ApplicationSystem): Lomake = {
    new Lomake(applicationSystem.getId, applicationSystem.getForm, applicationSystem.getAdditionalInformationElements.toList)
  }
}
