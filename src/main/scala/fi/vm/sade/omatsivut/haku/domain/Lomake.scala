package fi.vm.sade.omatsivut.haku.domain

import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Form, Element}

case class Lomake(oid: String, additionalInformation: List[Element], form: Form)

object Lomake {
  import scala.collection.JavaConverters._

  def apply(applicationSystem: ApplicationSystem): Lomake = {
    new Lomake(applicationSystem.getId, applicationSystem.getAdditionalInformationElements.asScala.toList, applicationSystem.getForm)
  }
}
