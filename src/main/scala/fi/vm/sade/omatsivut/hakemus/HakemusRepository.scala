package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut._
import com.mongodb.casbah.Imports._
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

  def getDelimiter(s: String) = if(s.contains("_")) "_" else "-"

  def updateHakemus(hakemus: Hakemus) {
    def clearPrevValues(hakemus: Hakemus) = {
      val query = MongoDBObject("oid" -> hakemus.oid)
      hakemukset.findOne(query).toList.map((hakemus: DBObject) => {
        val toiveet = hakemus.expand[Map[String, String]]("answers.hakutoiveet").getOrElse(Map()).toList
        toiveet.map { case (key, value) => ("answers.hakutoiveet." + key, "") }
      }).head
    }

    def getUpdates(hakemus: Hakemus) = {
      hakemus.hakutoiveet.zipWithIndex.flatMap {
        (t) => t._1.map {
          (elem) => ("answers.hakutoiveet.preference" + (t._2 + 1) + getDelimiter(elem._1) + elem._1, elem._2)
        }
      }.toMap[String, String]
    }

    def updateValues(hakemus: Hakemus, newData: List[(String, String)]) = {
      // TODO validation and identity check
      val query = MongoDBObject("oid" -> hakemus.oid)
      val update = $set(newData:_*)
      hakemukset.update(query, update)
    }

    val clearedValues = clearPrevValues(hakemus)
    val updates = getUpdates(hakemus)
    val combined = clearedValues ++ updates
    updateValues(hakemus, combined.toList)
  }

  def fetchHakemukset(oid: String): List[Hakemus] = {

    val query = MongoDBObject("personOid" -> oid)

    def shortenKey(v: (String, String), delimiter: Char = '-') = {
      v._1.substring(v._1.indexOf(delimiter) + 1)
    }

    def tupleWithShortKey(v: (String, String)) = {
      if (v._1.contains("_")) (shortenKey(v, '_'), v._2) else (shortenKey(v), v._2)
    }

    def shortenNames(tuple: (String, Map[String, String])) = {
      tuple._2.map(tupleWithShortKey) ++ Map("priority" -> tuple._1)
    }

    def groupPreferences(toiveet: Map[String, String]) = {
      val pattern = "preference(\\d+).*".r
      toiveet.groupBy((key) => key._1 match {
        case pattern(x: String) => x
        case _ => ""
      })
    }

    def convertEmptyPreferences(toiveet: Map[String, String]) = {
      if (toiveet.getOrElse("Koulutus-id", "").length() == 0) {
        Map("priority" -> toiveet.getOrElse("priority", ""))
      } else {
        toiveet
      }
    }

    def flatten(toiveet: Map[String, String]): List[Map[String, String]] = {
      groupPreferences(toiveet)
        .toList
        .map(shortenNames)
        .map(convertEmptyPreferences)
        .sortBy(map => map.get("priority"))
        .map((m) => m.filterKeys { Set("priority").contains(_) == false})
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
