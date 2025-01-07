package fi.vm.sade.hakemuseditori.hakemus.hakuapp


import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.{Criteria, Query}
import scala.jdk.CollectionConverters._

class ApplicationDao(mongoTemplate: MongoTemplate) {

  def findByPersonOid(personOid: String): List[Application] = {
    val query: Query = new Query();
    query.addCriteria(Criteria.where("personOid").is(personOid));
    val applications = mongoTemplate.find(query, classOf[Application]);
    applications.asScala.toList
  }

  def findByOid(oid: String): Option[Application] = {
    val query = new Query()
    query.addCriteria(Criteria.where("oid").is(oid))
    Option(mongoTemplate.findOne(query, classOf[Application]))
  }

}
