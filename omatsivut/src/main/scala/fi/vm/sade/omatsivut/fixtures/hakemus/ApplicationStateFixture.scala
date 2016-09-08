package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.omatsivut.fixtures.TestFixture

protected class ApplicationStateFixture(val dao: ApplicationDAO) {
  def setState(state: Application.State = Application.State.PASSIVE) {
    val application: Application = dao.find(new Application().setOid(TestFixture.hakemusYhteishakuKevat2014WithForeignBaseEducationId)).get(0)
    application.setState(state)
    dao.update(new Application().setOid(application.getOid), application)
  }

  def setPostProcessingState(state: Application.PostProcessingState) {
    val application: Application = dao.find(new Application().setOid(TestFixture.hakemusYhteishakuKevat2014WithForeignBaseEducationId)).get(0)
    application.setRedoPostProcess(state)
    dao.update(new Application().setOid(application.getOid), application)
  }
}
