package fi.vm.sade.omatsivut

import com.mongodb.casbah.Imports._
import org.slf4j.LoggerFactory

import com.mongodb.casbah.MongoCredential
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat._
import com.novus.salat.global._
import org.joda.time.DateTime

case class HakuAika(start: DateTime, end: DateTime)

case class Translations(translations: Map[String, String])

case class Haku(name: Translations, applicationPeriods: List[HakuAika])

case class Hakemus(oid: String, received: Long, var haku: Option[Haku] = None)

class HakemusRepository {

  RegisterJodaTimeConversionHelpers()

  val logger = LoggerFactory.getLogger(getClass())

  val settings = AppConfig.loadSettings
  val mongoClient = settings.hakuAppMongoClient
  val hakulomake = mongoClient.getDB(settings.hakuAppMongoDb)
  val hakemukset = hakulomake("application")
  val lomakkeet = hakulomake("applicationSystem")

  def fetchHakemukset(hetu: String) = {
    val query = MongoDBObject("answers.henkilotiedot.Henkilotunnus_plain" -> hetu)
    hakemukset.find(query).toList.map((hakemus: DBObject) => {
      val haku = getHaku(hakemus)
      (hakemus, haku)
    }).map((tuple: (DBObject, DBObject)) => {
      val hakem = grater[Hakemus].asObject(tuple._1)
      logger.info(tuple._2.toString)
      if (!tuple._2.isEmpty) {
        hakem.haku = Some(grater[Haku].asObject(tuple._2))
      }
      hakem
    })
  }

  private def getHaku(hakemus: DBObject): DBObject = {
    val hakuOid = hakemus.getAs[String]("applicationSystemId")
    val res = lomakkeet.findOne(MongoDBObject("_id" -> hakuOid), MongoDBObject("name" -> 1, "applicationPeriods" -> 1))
    logger.info("Got applications:" + res.toString)
    res match {
      case Some(x) => x.asDBObject
      case None => DBObject.empty
    }
  }
}
