package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat._
import com.novus.salat.global._
import org.joda.time.DateTime

object HakemusRepository extends Logging {
  RegisterJodaTimeConversionHelpers()

  private val settings = AppConfig.settings
  private val hakemukset = settings.hakuAppMongoDb("application")

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
    ApplicationDaoWrapper.findByPersonOid(oid)
  }

  def fetchHakemuksetOLD(oid: String): List[Hakemus] = {
    val query = MongoDBObject("personOid" -> oid)

    hakemukset.find(query).toList.map((applicationDbObject: DBObject) => {
      val application = grater[Hakemus].asObject(applicationDbObject)
      application.haku = HakuRepository.getApplicationSystemById(applicationDbObject.getAs[String]("applicationSystemId"))
      val toiveet = applicationDbObject.getAs[Map[String, String]]("answers").get.asDBObject.getAs[Map[String, String]]("hakutoiveet").get
      application.hakutoiveet = HakutoiveetConverter.convert(toiveet)
      application
    })
  }
}
