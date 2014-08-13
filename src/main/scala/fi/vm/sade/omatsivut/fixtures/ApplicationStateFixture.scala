package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Hakemus._

case class ApplicationStateFixture(appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO
  def apply(state: Application.State = Application.State.PASSIVE) {
    val application: Application = dao.find(new Application().setOid(TestFixture.hakemus2)).get(0)
    application.setState(state)
    dao.save(application)
  }
}
