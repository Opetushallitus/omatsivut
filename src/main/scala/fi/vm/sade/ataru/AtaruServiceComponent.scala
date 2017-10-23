package fi.vm.sade.ataru

import java.util.concurrent.TimeUnit

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, EducationBackground, Hakemus}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.Method.GET
import org.http4s.client.blaze
import org.http4s.json4s.native.jsonExtract
import org.http4s.{Request, Uri}
import org.json4s.DefaultFormats

import scala.concurrent.duration.Duration
import scalaz.concurrent.Task

case class AtaruApplication(oid: String,
                            secret: String,
                            haku: String,
                            hakukohteet: List[String])

trait AtaruServiceComponent  {
  this: LomakeRepositoryComponent
    with TarjontaComponent =>

  val ataruService: AtaruService

  class StubbedAtaruService extends AtaruService {
    override def findApplications(personOid: String): List[HakemusInfo] = List()
  }

  class RemoteAtaruService(config: AppConfig) extends AtaruService {
    private val blazeHttpClient = blaze.defaultClient
    private val casClient = new CasClient(config.settings.securitySettings.casUrl, blazeHttpClient)
    private val casParams = CasParams(
      OphUrlProperties.url("url-ataru-service"),
      "auth/cas",
      config.settings.securitySettings.casUsername,
      config.settings.securitySettings.casPassword
    )
    private val httpClient = CasAuthenticatingClient(
      casClient,
      casParams,
      blazeHttpClient,
      Some("omatsivut.omatsivut.backend"),
      "ring-session"
    )

    implicit val formats = DefaultFormats

    private def getApplications(personOid: String): List[AtaruApplication] = {
      Uri.fromString(OphUrlProperties.url("ataru-service.applications", personOid))
        .fold(Task.fail, uri => {
          httpClient.fetch(Request(method = GET, uri = uri)) {
            case r if r.status.code == 200 => r.as[List[AtaruApplication]](jsonExtract[List[AtaruApplication]])
            case r => Task.fail(new RuntimeException(s"Failed to get applications for $personOid: ${r.toString()}"))
          }
        }).attemptRunFor(Duration(10, TimeUnit.SECONDS)).fold(throw _, x => x)
    }

    def findApplications(personOid: String): List[HakemusInfo] = {
      getApplications(personOid)
        .map(a => (a, tarjontaService.haku(a.haku, Language.fi)))
        .collect {
          case (a, Some(haku)) =>
            val hakemus = Hakemus(
              a.oid,
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
        }
    }
  }
}

trait AtaruService {
  def findApplications(personOid: String): List[HakemusInfo]
}
