package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._

class ApplicationStateFixture(appConfig: AppConfig) {
  private val dao = appConfig.springContext.applicationDAO
  def setState(state: Application.State = Application.State.PASSIVE) {
    val application: Application = dao.find(new Application().setOid(TestFixture.hakemusYhteishakuKevat2014WithForeignBaseEducationId)).get(0)
    application.setState(state)
    dao.save(application)
  }

  def setPostProcessingState(state: Application.PostProcessingState) {
    val application: Application = dao.find(new Application().setOid(TestFixture.hakemusYhteishakuKevat2014WithForeignBaseEducationId)).get(0)
    application.setRedoPostProcess(state)
    dao.save(application)
  }
}
