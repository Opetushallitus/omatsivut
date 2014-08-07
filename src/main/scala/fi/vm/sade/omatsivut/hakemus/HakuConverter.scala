package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.lomake.domain.{ApplicationPeriod, ApplicationSystem}
import fi.vm.sade.omatsivut.domain.{Haku, HakuAika}
import org.joda.time.DateTime
import scala.collection.JavaConversions._
import fi.vm.sade.omatsivut.domain.Language

object HakuConverter {
  def convertToHaku(applicationSystem: ApplicationSystem)(implicit lang: Language.Language) = {
    val hakuAjat = applicationSystem.getApplicationPeriods.toList.map(applicationPeriod => convertToHakuAika(applicationPeriod))
    val korkeakouluhaku = applicationSystem.getKohdejoukkoUri == "haunkohdejoukko_12"
    Haku(applicationSystem.getId, convertTranslations(applicationSystem), hakuAjat, korkeakouluhaku)
  }

  private def convertTranslations(applicationSystem: ApplicationSystem)(implicit lang: Language.Language): String = {
    applicationSystem.getName.getTranslations.get(lang.toString())
  }

  private def convertToHakuAika(applicationPeriod: ApplicationPeriod): HakuAika = {
    HakuAika(new DateTime(applicationPeriod.getStart), new DateTime(applicationPeriod.getEnd))
  }
}
