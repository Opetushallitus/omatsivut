package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.{ValintatulosServiceRunner, PersonOid}
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.hakemus.domain.{Hakemus, HakuPaattynyt}
import fi.vm.sade.omatsivut.servlet.ClientSideVastaanotto
import fi.vm.sade.omatsivut.valintatulokset.RemoteValintatulosService
import org.json4s.jackson._

class VastaanottoSpec extends HakemusApiSpecification with FixturePerson {
  override implicit lazy val appConfig = new AppConfig.IT
  sequential
  addServlet(componentRegistry.newApplicationsServlet, "/secure/applications")

  "POST /applications/vastaanota/:hakuOid/:hakemusOid" should {
    "vastaanottaa paikan" in {
      ValintatulosServiceRunner.withValintatulosService {
        new RemoteValintatulosService(appConfig.settings.valintaTulosServiceUrl).applyFixture("hyvaksytty-kesken-julkaistavissa")

        authPost("/secure/applications/vastaanota/1.2.246.562.5.2013080813081926341928/1.2.246.562.11.00000441369", Serialization.write(ClientSideVastaanotto("1.2.246.562.5.72607738902", "VASTAANOTTANUT"))) {
          status must_== 200
        }
      }
    }

    "hylkää pyynnön väärältä henkilöltä" in {
      ValintatulosServiceRunner.withValintatulosService {
        new RemoteValintatulosService(appConfig.settings.valintaTulosServiceUrl).applyFixture("hyvaksytty-kesken-julkaistavissa")

        authPost("/secure/applications/vastaanota/1.2.246.562.5.2013080813081926341928/1.2.246.562.11.00000441369", Serialization.write(ClientSideVastaanotto("1.2.246.562.5.72607738902", "VASTAANOTTANUT"))) {
          status must_== 404
        }(PersonOid("WRONG PERSON"))
      }
    }
  }
}
