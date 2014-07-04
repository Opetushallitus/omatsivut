package fi.vm.sade.omatsivut.hakemus

import com.mongodb.casbah.Imports._
import com.novus.salat._
import scala.Some
import fi.vm.sade.omatsivut.AppConfig
import com.novus.salat._
import com.novus.salat.global._

object HakuRepository {
  private val settings = AppConfig.settings
  private val lomakkeet = settings.hakuAppMongoDb("applicationSystem")

  def getApplicationSystemById(hakuOid: Option[String]): Option[Haku] = {
    val res = lomakkeet.findOne(MongoDBObject("_id" -> hakuOid), MongoDBObject("name" -> 1, "applicationPeriods" -> 1))
    res match {
      case Some(x) => Some(grater[Haku].asObject(x.asDBObject))
      case None => None
    }
  }
}
