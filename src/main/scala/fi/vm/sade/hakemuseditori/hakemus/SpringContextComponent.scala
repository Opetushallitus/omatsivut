package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.haku.oppija.common.organisaatio.OrganizationService
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.oppija.hakemus.service.impl.SendMailService
import fi.vm.sade.haku.oppija.hakemus.service.{HakumaksuService, SyntheticApplicationService, ApplicationService}
import fi.vm.sade.haku.oppija.lomake.service.ApplicationSystemService
import fi.vm.sade.haku.oppija.lomake.validation.ElementTreeValidator
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.MongoTemplate

trait SpringContextComponent {
  val springContext: HakemusSpringContext
}

class HakemusSpringContext(context: ApplicationContext) {
  def applicationSystemService = context.getBean(classOf[ApplicationSystemService])

  def applicationDAO = context.getBean(classOf[ApplicationDAO])

  def applicationService = context.getBean(classOf[ApplicationService])

  def mongoTemplate = context.getBean(classOf[MongoTemplate])

  def validator = context.getBean(classOf[ElementTreeValidator])

  def organizationService = context.getBean(classOf[OrganizationService])

  def syntheticApplicationService = context.getBean(classOf[SyntheticApplicationService])

  def paymentService = context.getBean(classOf[HakumaksuService])

  def sendMailService = context.getBean(classOf[SendMailService])
}