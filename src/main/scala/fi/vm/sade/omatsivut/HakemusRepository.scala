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

case class Hakemus(oid: String, received: Long, var hakutoiveet: List[Map[String, String]] = Nil, var haku: Option[Haku] = None)

object HakemusRepository extends Logging {

  RegisterJodaTimeConversionHelpers()

  private val settings = AppConfig.loadSettings
  private val hakemukset = settings.hakuAppMongoDb("application")
  private val lomakkeet = settings.hakuAppMongoDb("applicationSystem")

  def fetchHakemukset(oid: String): List[Hakemus] = {

    val query = MongoDBObject("personOid" -> oid)

    def shortenKey(v: (String, String), delimiter: Char = '-'): String = {
      v._1.substring(v._1.indexOf(delimiter) + 1)
    }

    def tupleWithShortKey(v: (String, String)): (String, String) = {
      if (v._1.contains("_")) (shortenKey(v, '_'), v._2) else (shortenKey(v), v._2)
    }

    def groupPreferences(toiveet: Map[String, String]): Map[String, Map[String, String]] = {
      val pattern = "preference(\\d+).*".r
      toiveet.groupBy((key) => key._1 match {
        case pattern(x) => x
        case _ => ""
      })
    }

    def flatten(toiveet: Map[String, String]): List[Map[String, String]] = {
      groupPreferences(toiveet).toList.map((tuple) => tuple._2.map((v) => tupleWithShortKey(v)) ++ Map("priority" -> tuple._1))
    }

    hakemukset.find(query).toList.map((hakemus: DBObject) => {
      val haku = getHaku(hakemus)
      (hakemus, haku)
    }).map((tuple: (DBObject, DBObject)) => {
      val hakem = grater[Hakemus].asObject(tuple._1)
      val toiveet = tuple._1.getAs[Map[String, String]]("answers").get.asDBObject.getAs[Map[String, String]]("hakutoiveet").get
      hakem.hakutoiveet = flatten(toiveet)
      if (!tuple._2.isEmpty) {
        hakem.haku = Some(grater[Haku].asObject(tuple._2))
      }
      hakem
    })
  }

  private def getHaku(hakemus: DBObject): DBObject = {
    val hakuOid = hakemus.getAs[String]("applicationSystemId")
    val res = lomakkeet.findOne(MongoDBObject("_id" -> hakuOid), MongoDBObject("name" -> 1, "applicationPeriods" -> 1))
    res match {
      case Some(x) => x.asDBObject
      case None => DBObject.empty
    }
  }
}
