package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.haku.oppija.common.organisaatio.OrganizationService
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.MongoTemplate

trait SpringContextComponent {
  val springContext: HakemusSpringContext
}

class HakemusSpringContext(context: ApplicationContext) {

  def mongoTemplate = context.getBean(classOf[MongoTemplate])

  def organizationService = context.getBean(classOf[OrganizationService])

}
