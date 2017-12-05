package fi.vm.sade.ataru

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, EducationBackground, Hakemus}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.utils.http.HttpClient
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.DefaultFormats
import org.json4s.native.JsonMethods

case class AtaruApplication(key: String, state: String, haku: String, secret: String) {
  def passive: Boolean = state == "canceled"
}

trait AtaruServiceComponent  {
  this: LomakeRepositoryComponent
    with TarjontaComponent =>

  val ataruService: AtaruService

  class StubbedAtaruService extends AtaruService {
    override def findApplications(personOid: String): Either[Throwable, List[HakemusInfo]] = Right(List())
  }

  class RemoteAtaruService(httpClient: HttpClient) extends AtaruService with Logging {

    implicit val formats = DefaultFormats

    private def getApplications(personOid: String): Either[Throwable, List[AtaruApplication]] = {
      httpClient
        .httpGet(OphUrlProperties.url("ataru.applications.modify", personOid))
        .responseWithHeaders() match {
        case (200, _, body) =>
          Right(JsonMethods.parse(body).extract[List[AtaruApplication]])
        case (status, _, body) =>
          logger.info(s"Failed to get applications by person OID from Ataru service, HTTP status code: $status, response body: $body")
          Left(new RuntimeException(s"Failed to get applications by person OID from Ataru service"))
      }
    }

    def findApplications(personOid: String): Either[Throwable, List[HakemusInfo]] = {
      getApplications(personOid)
        .right.map(_.filterNot(_.passive)
          .map(a => (a, tarjontaService.haku(a.haku, Language.fi)))
          .collect {
            case (a, Some(haku)) =>
              val hakemus = Hakemus(
                a.key,
                Option(System.currentTimeMillis()),
                None,
                Active(),
                None,
                List(),
                haku,
                EducationBackground("base_education", false),
                Map(),
                Option("Helsinki"),
                false,
                true,
                None,
                Map(),
                Option(a.secret))
              HakemusInfo(hakemus, List(), List(), true, None, "Ataru", OphUrlProperties.url("ataru.hakija.url"))
          })
    }
  }
}

trait AtaruService {
  def findApplications(personOid: String): Either[Throwable,List[HakemusInfo]]
}
