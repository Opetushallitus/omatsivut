package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import collection.JavaConversions._

object TestFixture {
  val personOid = "1.2.246.562.24.14229104472"

  lazy val (as, app) = {
    (new AppConfig.IT).withConfig { appConfig =>
      val as = appConfig.springContext.applicationSystemService.getApplicationSystem("1.2.246.562.5.2014022711042555034240")
      val app = appConfig.springContext.applicationDAO.find(new Application().setPersonOid(personOid)).toList.head
      (as, app)
    }
  }

  def applicationSystem: ApplicationSystem = as
  def haku = HakuConverter.convertToHaku(applicationSystem)
  def application: Application = app
  def hakemus = HakemusConverter.convertToHakemus(Some(haku))(application)

  val hakutoive: Hakutoive = JsonFixtureMaps.find[Hakutoive]("/mockdata/hakutoiveet.json", "1.2.246.562.14.2014030415375012208392")

}
