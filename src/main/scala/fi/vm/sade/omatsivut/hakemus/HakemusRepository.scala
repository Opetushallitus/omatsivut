package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import com.novus.salat._
import com.novus.salat.global._
import org.joda.time.DateTime

object HakemusRepository extends Logging {
  RegisterJodaTimeConversionHelpers()

  private val hakemukset = AppConfig.settings.hakuAppMongoDb("application")

  def updateHakemus(hakemus: Hakemus) {
    ApplicationDaoWrapper.updateApplication(hakemus)
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
