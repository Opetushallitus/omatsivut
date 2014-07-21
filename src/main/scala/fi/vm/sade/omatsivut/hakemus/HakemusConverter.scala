package fi.vm.sade.omatsivut.hakemus

import java.util

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain.{EducationBackground, Hakemus, Haku}

import scala.collection.JavaConversions._
import scala.util.Try

object HakemusConverter {
  def convertToHakemus(haku: Option[Haku])(application: Application) = {
    val koulutusTaustaAnswers: util.Map[String, String] = application.getAnswers.get("koulutustausta")
    Hakemus(
      application.getOid,
      application.getReceived.getTime,
      application.getUpdated.getTime,
      convertHakuToiveet(application),
      haku,
      EducationBackground(koulutusTaustaAnswers.get("POHJAKOULUTUS"), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false))
    )
  }

  private def convertHakuToiveet(application: Application): List[Hakutoive] = {
    val answers: util.Map[String, util.Map[String, String]] = application.getAnswers
    val hakuToiveetData: Map[String, String] = answers.get("hakutoiveet").toMap
    HakutoiveetConverter.convertFromAnswers(hakuToiveetData)
  }
}
