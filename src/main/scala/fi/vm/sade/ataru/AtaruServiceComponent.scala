package fi.vm.sade.ataru

import java.util.UUID

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, EducationBackground, Hakemus}
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, ImmutableLegacyApplicationWrapper}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.haku.oppija.hakemus.domain.Application

trait AtaruServiceComponent  {
  this: LomakeRepositoryComponent
    with TarjontaComponent =>

  def newAtaruService(): AtaruService = {
    new AtaruService()
  }

  class AtaruService {
    private def getApplications(): List[Application] = {
      val application = new Application()
      application.activate()
      application.setOid(UUID.randomUUID().toString())
      application.setApplicationSystemId("1.2.246.562.29.95390561488") // haku OID
      List(application)
    }

    def findApplications(personOid: String): List[HakemusInfo] = {
      getApplications()
        .map(ImmutableLegacyApplicationWrapper.wrap)
        .filter(a => !a.state.equals("PASSIVE"))
        .map(a => {
          val haku = tarjontaService.haku(a.hakuOid, Language.fi)
          val hakemus = Hakemus(
            a.oid,
            Option(System.currentTimeMillis()),
            None,
            Active(),
            None,
            List(),
            haku.get,
            EducationBackground("base_education", false),
            Map(),
            Option("Helsinki"),
            false,
            false,
            None,
            Map())
          HakemusInfo(hakemus, List(), List(), true, None)
        })
        .filter(a => a != null)
    }
  }
}
