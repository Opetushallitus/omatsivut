package fi.vm.sade.hakemuseditori.oppijanumerorekisteri

import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.Method.GET
import org.http4s.client.blaze
import org.http4s.{Request, Uri}
import org.json4s.JsonAST.{JNull, JObject, JString, JValue}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Reader, Writer}

import scala.concurrent.duration.Duration
import scalaz.concurrent.Task

case class Henkilo(oid: String, hetu: Option[String])

object Henkilo {
  implicit private val formats = DefaultFormats
  val henkiloReader = new Reader[Henkilo] {
    override def read(value: JValue): Henkilo = {
      Henkilo(
        (value \ "oidHenkilo").extract[String],
        (value \ "hetu").extractOpt[String]
      )
    }
  }
  val henkiloWriter = new Writer[Henkilo] {
    override def write(h: Henkilo): JValue = {
      JObject(
        "oidHenkilo" -> JString(h.oid.toString),
        "hetu" -> h.hetu.map(JString).getOrElse(JNull)
      )
    }
  }
}

trait OppijanumerorekisteriService {
  def henkilo(personOid: String): Henkilo
}

trait OppijanumerorekisteriComponent {
  val oppijanumerorekisteriService: OppijanumerorekisteriService

  class StubbedOppijanumerorekisteriService extends OppijanumerorekisteriService {
    override def henkilo(personOid: String): Henkilo = ???
  }

  class RemoteOppijanumerorekisteriService(config: AppConfig) extends OppijanumerorekisteriService {
    private val blazeHttpClient = blaze.defaultClient
    private val casClient = new CasClient(config.settings.securitySettings.casUrl, blazeHttpClient)
    private val casParams = CasParams(
      OphUrlProperties.url("url-oppijanumerorekisteri-service"),
      config.settings.securitySettings.casUsername,
      config.settings.securitySettings.casPassword
    )
    private val httpClient = CasAuthenticatingClient(
      casClient,
      casParams,
      blazeHttpClient,
      Some("omatsivut.omatsivut.backend"),
      "JSESSIONID"
    )
    implicit private val formats = DefaultFormats
    implicit private val henkiloReader = Henkilo.henkiloReader

    override def henkilo(personOid: String): Henkilo = {
      Uri.fromString(OphUrlProperties.url("oppijanumerorekisteri-service.henkiloByOid", personOid))
        .fold(Task.fail, uri => {
          httpClient.fetch(Request(method = GET, uri = uri)) {
            case r if r.status.code == 200 => r.as[String].map(s => JsonMethods.parse(s).as[Henkilo])
            case r => Task.fail(new RuntimeException(s"Failed to get henkilö for $personOid: ${r.toString()}"))
          }
        }).attemptRunFor(Duration(10, TimeUnit.SECONDS)).fold(throw _, x => x)
    }
  }
}