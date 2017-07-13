package fi.vm.sade.ataru

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, EducationBackground, Hakemus}
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, ImmutableLegacyApplicationWrapper}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.hakemus.domain.Application.State
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.http.HttpClient
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods

case class AtaruApplication(key: String, state: String, haku: String, secret: String)

trait AtaruServiceComponent  {
  this: LomakeRepositoryComponent
    with TarjontaComponent =>

  def newAtaruService(httpClient: HttpClient): AtaruService = {
    new AtaruService(httpClient)
  }

  implicit val formats = DefaultFormats

  class AtaruService(httpClient: HttpClient) {

    private def getState(ataruApplication: AtaruApplication): State = {
      ataruApplication.state match {
        case "application-has-accepted" => State.SUBMITTED
        case "selected" => State.SUBMITTED
        case "not-selected" => State.PASSIVE
        case "canceled" => State.PASSIVE
        case _ => State.INCOMPLETE
      }
    }

    private def getApplications(personOid: String): List[(Application, String)] = {
      httpClient
        .httpGet(OphUrlProperties.url("ataru.applications.modify", personOid))
        .responseWithHeaders() match {
        case (200, _, body) => {
          JsonMethods
            .parse(body)
            .extract[List[AtaruApplication]]
            .map(aa => {
              val application = new Application()
              application.setOid(aa.key)
              application.setApplicationSystemId(aa.haku) // haku OID
              application.setState(getState(aa))
              (application, aa.secret)
            })
        }
        case (status, _, body) =>
          throw new RuntimeException(s"Failed to get applications by person OID from Ataru service, HTTP status code: $status, response body: $body")
      }
    }

    def findApplications(personOid: String): List[HakemusInfo] = {
      getApplications(personOid)
        .map { case (a: Application, s: String) => (ImmutableLegacyApplicationWrapper.wrap(a), s) }
        .filter { case (a: ImmutableLegacyApplicationWrapper, _) => !a.state.equals("PASSIVE") }
        .map { case (a: ImmutableLegacyApplicationWrapper, s: String) => {
          val haku: Option[Haku] = tarjontaService.haku(a.hakuOid, Language.fi)
          if (haku.nonEmpty) {
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
              true,
              None,
              Map(),
              Option(s))
            HakemusInfo(hakemus, List(), List(), true, None, "Ataru", OphUrlProperties.url("ataru.hakija.url"))
          } else {
            null
          }
        }}
        .filter(a => a != null)
    }
  }
}
