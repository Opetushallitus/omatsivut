package fi.vm.sade.omatsivut.fixtures.hakemus

import fi.vm.sade.hakemuseditori.hakemus.HakemusSpringContext
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.SyntheticApplication.Hakemus
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.SyntheticApplication
import fi.vm.sade.haku.testfixtures.MongoFixtureImporter
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.hakemuseditori.valintatulokset.RemoteValintatulosService

class ErillishakuFixtureImporter(appConfig: AppConfig, springContext: HakemusSpringContext) {
  def applyFixtures(hyvaksytty: Boolean) {
    import collection.JavaConversions._
    //MongoFixtureImporter.clearFixtures(springContext.mongoTemplate, springContext.applicationDAO, "application")

    val hakemus = new Hakemus(
      TestFixture.personOid,
      "Erillis",
      "Hakija",
      OppijaConstants.SUKUPUOLI_MIES,
      OppijaConstants.EDUCATION_LANGUAGE_FI,
      "010100A939R",
      "foobar@example.com",
      "9.1.1995",
      "fi",
      "040123456",
      "Tie 2",
      "00100",
      "HELSINKI",
      "FIN",
      "FIN",
      "091",
      false,
      "FIN",
      "NOT_REQUIRED"
    )

    val hakukohde: String = "1.2.246.562.5.72607738902"
    val tarjoaja: String = "1.2.246.562.10.591352080610"
    val syntheticApplication = new SyntheticApplication(hakukohde, "korkeakoulu-erillishaku", tarjoaja, List(hakemus))
    // TODO mockaa!
    //springContext.syntheticApplicationService.createApplications(syntheticApplication)

    val fixtureName: String = if(hyvaksytty) "hyvaksytty-korkeakoulu-erillishaku" else "hylatty-korkeakoulu-erillishaku"
    new RemoteValintatulosService().applyFixtureWithQuery(Map(
      "fixturename" -> fixtureName,
      "haku" -> "korkeakoulu-erillishaku",
      "useHakuAsHakuOid" -> "true",
      "ohjausparametrit" -> "varasijasaannot-ei-viela-voimassa"))
  }
}
