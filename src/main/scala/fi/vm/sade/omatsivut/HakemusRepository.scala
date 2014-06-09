package fi.vm.sade.omatsivut

import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory

class HakemusRepository {

  case class Haku(name: String) {}

  val logger = LoggerFactory.getLogger(getClass())

  val mongoClient = MongoClient("localhost", 27017)
  val hakulomake = mongoClient.getDB("hakulomake")
  val hakemukset = hakulomake("application")
  val lomakkeet = hakulomake("applicationSystem")

  def fetchHakemukset(hetu: String): List[Haku] = {
    val query = MongoDBObject("answers.henkilotiedot.Henkilotunnus_plain" -> hetu)
    hakemukset.find(query).toList.map((res: DBObject) => getHaku(res.getAs[String]("applicationSystemId").get))
  }

  private def getHaku(oid: String): Haku = {
    val res = lomakkeet.findOne(MongoDBObject("_id" -> oid), MongoDBObject("form.i18nText" -> 1))
    res match {
      case Some(res) => Haku(res.asDBObject.getAs[DBObject]("form").get.asDBObject.getAs[DBObject]("i18nText").get.asDBObject.getAs[DBObject]("translations").get.asDBObject.getAs[String]("fi").getOrElse(""))
      case None => Haku("")
    }
  }
}
