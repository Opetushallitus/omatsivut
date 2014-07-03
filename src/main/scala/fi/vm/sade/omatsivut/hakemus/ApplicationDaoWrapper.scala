package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.it.dao.impl.ApplicationDAOMongoImpl
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import scala.collection.JavaConversions._
import fi.vm.sade.omatsivut.OmatSivutSpringContext

object ApplicationDaoWrapper {
  def findByPersonOid(personOid: String): List[Hakemus] = {
    val dao: ApplicationDAOMongoImpl = OmatSivutSpringContext.context.getBean(classOf[ApplicationDAOMongoImpl])
    val applicationJavaObjects: List[Application] = dao.find(new Application()).toList
    applicationJavaObjects.map { application =>
      Hakemus(application.getOid, application.getReceived.getTime, Nil, None)
    }
  }
}