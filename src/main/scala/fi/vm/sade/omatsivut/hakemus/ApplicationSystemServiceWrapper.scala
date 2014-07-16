package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.{ApplicationPeriod, ApplicationSystem}
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.{Haku, HakuAika, Translations}
import org.joda.time.DateTime

import scala.collection.JavaConversions._

case class ApplicationSystemServiceWrapper(implicit val appConfig: AppConfig) {
  private val repository = appConfig.springContext.applicationSystemService

  def findByOid(applicationSystemOid: String): Option[Haku] = {
    tryFind(applicationSystemOid).map { applicationSystem =>
      val hakuAjat = applicationSystem.getApplicationPeriods.toList.map(applicationPeriod => convertToHakuAika(applicationPeriod))
      Haku(applicationSystem.getId, convertTranslations(applicationSystem), hakuAjat)
    }
  }

  private def convertTranslations(applicationSystem: ApplicationSystem): Translations = {
    Translations(applicationSystem.getName.getTranslations.toMap)
  }

  private def convertToHakuAika(applicationPeriod: ApplicationPeriod): HakuAika = {
    HakuAika(new DateTime(applicationPeriod.getStart), new DateTime(applicationPeriod.getEnd))
  }

  private def tryFind(applicationSystemOid: String): Option[ApplicationSystem] = {
    try {
      Some(repository.getApplicationSystem(applicationSystemOid))
    } catch {
      case e: Exception => None
    }
  }
}
