package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.{ApplicationPeriod, ApplicationSystem}
import fi.vm.sade.omatsivut.domain.{Haku, HakuAika}
import org.joda.time.DateTime

import scala.collection.JavaConversions._

object HakuConverter {
  def convertToHaku(applicationSystem: ApplicationSystem) = {
    val hakuAjat = applicationSystem.getApplicationPeriods.toList.map(applicationPeriod => convertToHakuAika(applicationPeriod))
    Haku(applicationSystem.getId, convertTranslations(applicationSystem), hakuAjat)
  }

  private def convertTranslations(applicationSystem: ApplicationSystem): String = {
    applicationSystem.getName.getTranslations.get("fi") // TODO: kieliversiot
  }

  private def convertToHakuAika(applicationPeriod: ApplicationPeriod): HakuAika = {
    HakuAika(new DateTime(applicationPeriod.getStart), new DateTime(applicationPeriod.getEnd))
  }
}
