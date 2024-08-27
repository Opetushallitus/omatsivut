package fi.vm.sade.hakemuseditori.hakemus

import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.MongoTemplate

trait SpringContextComponent {
  val springContext: HakemusSpringContext
}

class HakemusSpringContext(context: ApplicationContext) {

  def mongoTemplate = context.getBean(classOf[MongoTemplate])


}
