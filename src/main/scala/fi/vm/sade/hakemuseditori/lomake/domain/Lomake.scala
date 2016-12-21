package fi.vm.sade.hakemuseditori.lomake.domain

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Element, Form}
import fi.vm.sade.hakemuseditori.hakemus.ImmutableLegacyApplicationWrapper
import fi.vm.sade.hakemuseditori.lomake.ElementWrapper

import scala.collection.JavaConversions._

case class Lomake(oid: String, form: Form, private val additionalInformation: List[Element], maxHakutoiveet: Int, baseEducationDoesNotRestrictApplicationOptions: Boolean) {
  def requiresAdditionalInfo(application: ImmutableLegacyApplicationWrapper): Boolean = {
    /*
    // Other possible ways to write this and to make it a bit more clear
    val solution1 = !(for(addInfo <- additionalInformation)
      yield ElementWrapper.wrapFiltered(addInfo, application.flatAnswers)
      ).filterNot(_.children.isEmpty).isEmpty
    val solution2 = !additionalInformation.map(addInfo => ElementWrapper.wrapFiltered(addInfo, application.flatAnswers)).forall(_.children.isEmpty)
    val solution3 = !additionalInformation.forall(ElementWrapper.wrapFiltered(_, application.flatAnswers).children.isEmpty)
    */
    !application.attachments.isEmpty ||
      !(for(addInfo <- additionalInformation)
      yield ElementWrapper.wrapFiltered(addInfo, application.flatAnswers)
        ).filterNot(_.children.isEmpty).isEmpty
  }
}

object Lomake {
  def apply(applicationSystem: ApplicationSystem): Lomake = {
    new Lomake(applicationSystem.getId,
      applicationSystem.getForm,
      applicationSystem.getAdditionalInformationElements.toList,
      applicationSystem.getMaxApplicationOptions,
      applicationSystem.baseEducationDoesNotRestrictApplicationOptions()
    )
  }
}
