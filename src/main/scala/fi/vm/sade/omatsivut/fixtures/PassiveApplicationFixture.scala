package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Hakemus._

case class PassiveApplicationFixtureFixture(appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO
  def apply {
    val application: Application = dao.find(new Application().setOid(TestFixture.hakemus2)).get(0)
    application.setState(Application.State.PASSIVE)
    dao.save(application)
  }
}
