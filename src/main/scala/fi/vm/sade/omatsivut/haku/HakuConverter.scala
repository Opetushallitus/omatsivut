package fi.vm.sade.omatsivut.haku

import fi.vm.sade.haku.oppija.lomake.domain.{ApplicationPeriod, ApplicationSystem}
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.haku.domain.{HakuAika, Haku}

import scala.collection.JavaConversions._

object HakuConverter {
  def convertToHaku(applicationSystem: ApplicationSystem)(implicit lang: Language.Language) = {
    val hakuAjat = convertToHakuajat(applicationSystem)
    val korkeakouluhaku = applicationSystem.getKohdejoukkoUri == "haunkohdejoukko_12"
    Haku(applicationSystem.getId, convertTranslations(applicationSystem), hakuAjat, korkeakouluhaku)
  }

  def convertToHakuajat(applicationSystem: ApplicationSystem) = {
    applicationSystem.getApplicationPeriods.toList.map(applicationPeriod => convertToHakuAika(applicationPeriod))
  }

  private def convertTranslations(applicationSystem: ApplicationSystem)(implicit lang: Language.Language): String = {
    applicationSystem.getName.getTranslations.get(lang.toString())
  }

  private def convertToHakuAika(applicationPeriod: ApplicationPeriod): HakuAika = {
    HakuAika(applicationPeriod.getStart.getTime, applicationPeriod.getEnd.getTime)
  }
}
