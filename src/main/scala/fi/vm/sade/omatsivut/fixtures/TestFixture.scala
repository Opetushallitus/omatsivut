package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.{HakemusConverter, HakuConverter}

import scala.collection.JavaConversions._

object TestFixture {
  val hakemus1 = "1.2.246.562.11.00000877107"
  val hakemus2 = "1.2.246.562.11.00000441368"
  val applicationSystemOid = "1.2.246.562.5.2014022711042555034240"
  val personOid = "1.2.246.562.24.14229104472"
  val testHetu = "010101-123N"

  lazy val (as, app) = {
    (new AppConfig.IT).withConfig { appConfig =>
      val as = appConfig.springContext.applicationSystemService.getApplicationSystem(applicationSystemOid)
      val app = appConfig.springContext.applicationDAO.find(new Application().setOid(hakemus1)).toList.head
      (as, app)
    }
  }

  def applicationSystem: ApplicationSystem = as
  def haku = HakuConverter.convertToHaku(applicationSystem)
  def application: Application = app
  def hakemus = HakemusConverter.convertToHakemus(Some(haku))(application)

  val ammattistartti: Hakutoive = JsonFixtureMaps.find[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014030415375012208392")
  val ammattistarttiAhlman: Hakutoive = JsonFixtureMaps.find[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014040912353139913320")
  val hevostalous: Hakutoive = JsonFixtureMaps.find[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.5.31982630126")
}
