package fi.vm.sade.hakemuseditori.oppijanumerorekisteri

import java.util.concurrent.TimeUnit

import fi.vm.sade.groupemailer.Json4sHttp4s
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s.MediaType.`application/json`
import org.http4s.Method.GET
import org.http4s._
import org.http4s.client.blaze
import org.http4s.headers.Accept
import org.json4s
import org.json4s.JsonAST.{JNull, JObject, JString, JValue}
import org.json4s.jackson.JsonMethods
import org.json4s.{DefaultFormats, Reader, Writer, _}
import scalaz.concurrent.Task

import scala.concurrent.duration.Duration

case class Henkilo(oid: String, hetu: Option[String], oppijanumero: Option[String])

object Henkilo {
  implicit private val formats = DefaultFormats
  val henkiloReader = new Reader[Henkilo] {
    override def read(value: JValue): Henkilo = {
      Henkilo(
        (value \ "oidHenkilo").extract[String],
        (value \ "hetu").extractOpt[String],
        (value \ "oppijanumero").extractOpt[String]
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
  def fetchAllDuplicateOids(oppijanumero: String): Set[String]
}

trait OppijanumerorekisteriComponent {
  val oppijanumerorekisteriService: OppijanumerorekisteriService

  class StubbedOppijanumerorekisteriService extends OppijanumerorekisteriService {
    override def henkilo(personOid: String): Henkilo = {
      Henkilo(personOid, Some("kva kva"), Some(personOid))
    }

    override def fetchAllDuplicateOids(oppijanumero: String): Set[String] = Set(oppijanumero)
  }

  class RemoteOppijanumerorekisteriService(config: AppConfig, casVirkailijaClient: CasClient) extends OppijanumerorekisteriService with JsonFormats with Logging {
    private val casParams = CasParams(
      OphUrlProperties.url("url-oppijanumerorekisteri-service"),
      config.settings.securitySettings.casVirkailijaUsername,
      config.settings.securitySettings.casVirkailijaPassword
    )
    private val httpClient = CasAuthenticatingClient(
      casVirkailijaClient,
      casParams,
      blaze.defaultClient,
      AppConfig.callerId,
      "JSESSIONID"
    )
    private val callerIdHeader = Header("Caller-Id", AppConfig.callerId)
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

    private def uriFromString(url: String): Uri = {
      Uri.fromString(url).toOption.get
    }
    private def runHttp[ResultType](request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
      httpClient.fetch(request)(r => r.as[String].map(body => decoder(r.status.code, body, request)))
    }

    override def fetchAllDuplicateOids(oppijanumero: String): Set[String] = {
      logger.debug(s"Fetching duplicate oids for oppijanumero $oppijanumero")
      val timeout = Duration(30, TimeUnit.SECONDS)

      val body: json4s.JValue = Extraction.decompose(Map("henkiloOids" -> List(oppijanumero)))
      val duplicateHenkilosRequest = Request(
        method = Method.POST,
        uri = uriFromString(OphUrlProperties.url("oppijanumerorekisteri-service.duplicatesByPersonOids")),
        headers = Headers(callerIdHeader, `Accept`(`application/json`))
      ).withBody(body)(Json4sHttp4s.json4sEncoderOf)

      val henkiloviitteet: Seq[Henkiloviite] = httpClient.fetch(duplicateHenkilosRequest)((r: Response) =>
        if (r.status == Status.Ok) {
          r.as[String].map(parseHenkiloviiteResponse(_, oppijanumero))
        } else {
          logger.error("Failed to fetch henkiloviite data for user oid {}, response was {}, {}", oppijanumero, r.status, r.body)
          Task.now(Nil)
        }).runFor(timeout)
      val allHenkiloOids: Set[String] = henkiloviitteet.flatMap(viite => Set(viite.henkiloOid, viite.masterOid)).++(Seq(oppijanumero)).toSet
      if (allHenkiloOids.size > 1) {
        logger.info(s"Got ${allHenkiloOids.size} in total for oppijanumero $oppijanumero : $allHenkiloOids")
      }
      allHenkiloOids
    }

    private def parseHenkiloviiteResponse(responseBody: String, oppijanumero: String): Seq[Henkiloviite] = {
      try {
        JsonMethods.parse(responseBody).extract[Seq[Henkiloviite]]
      } catch {
        case e: Exception =>
          logger.error(s"Problem when parsing Henkiloviite list for $oppijanumero from response '$responseBody'", e)
          throw e
      }
    }
  }
}

case class Henkiloviite(henkiloOid: String, masterOid: String)
