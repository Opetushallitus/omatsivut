package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.dto.SyntheticApplication
import fi.vm.sade.haku.oppija.hakemus.domain.dto.SyntheticApplication.Hakemus
import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.OmatSivutSpringContext
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.valintatulokset.RemoteValintatulosService

class ErillishakuFixtureImporter(appConfig: AppConfig, springContext: OmatSivutSpringContext) {
  def applyFixtures(hyvaksytty: Boolean) {
    import collection.JavaConversions._
    MongoFixtureImporter.clearFixtures(springContext.mongoTemplate, springContext.applicationDAO, "application")
    val hakemus = new Hakemus(TestFixture.personOid, "Erillis", "Hakija", "010101-123N", "foobar@example.com", "9.1.1995")
    val hakukohde: String = "1.2.246.562.5.72607738902"
    val tarjoaja: String = "1.2.246.562.10.591352080610"
    val syntheticApplication = new SyntheticApplication(hakukohde, "korkeakoulu-erillishaku", tarjoaja, List(hakemus))
    springContext.syntheticApplicationService.createApplications(syntheticApplication)

    val fixtureName: String = if(hyvaksytty) "hyvaksytty-korkeakoulu-erillishaku" else "hylatty-korkeakoulu-erillishaku"
    new RemoteValintatulosService(appConfig.settings.valintaTulosServiceUrl).applyFixtureWithQuery("fixturename=" + fixtureName)
  }
}
