package fi.vm.sade.omatsivut

import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory

class HakemusRepository {

  case class Hakemus(hakemusOid: String, haunNimi: Map[String, String]) {

  }

  val logger = LoggerFactory.getLogger(getClass())

  val mongoClient = MongoClient("localhost", 27017)
  val hakulomake = mongoClient.getDB("hakulomake")
  val hakemukset = hakulomake("application")
  val lomakkeet = hakulomake("applicationSystem")

  def fetchHakemukset(hetu: String): List[Hakemus] = {
    val query = MongoDBObject("answers.henkilotiedot.Henkilotunnus_plain" -> hetu)
    hakemukset.find(query).map((res: DBObject) => getHaku(res.getAs[String]("applicationSystemId").get)).toList
  }

  private def getHaku(oid: String): Hakemus = {
    val res = lomakkeet.findOne(MongoDBObject("_id" -> oid), MongoDBObject("form.i18nText" -> 1))
    res match {
      case Some(res) => Hakemus(oid, res.asDBObject.getAs[DBObject]("form").get.asDBObject.getAs[DBObject]("i18nText").get.asDBObject.getAs[Map[String, String]]("translations").get)
      case None => Hakemus(oid, Map.empty)
    }
  }
}
