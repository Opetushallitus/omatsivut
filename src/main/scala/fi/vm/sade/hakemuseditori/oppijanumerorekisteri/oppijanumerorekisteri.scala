package fi.vm.sade.hakemuseditori.oppijanumerorekisteri

import java.util.concurrent.TimeUnit

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s.Method.GET
import org.http4s.client.blaze
import org.http4s.{Header, Headers, Request, Uri}
import org.json4s
import org.json4s._
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
  def fetchAllOids(pOid: String): List[String]
}

trait OppijanumerorekisteriComponent {
  val oppijanumerorekisteriService: OppijanumerorekisteriService

  class StubbedOppijanumerorekisteriService extends OppijanumerorekisteriService {
    override def henkilo(personOid: String): Henkilo = ???
    override def fetchAllOids(pOid: String): List[String] = List("1.2.246.562.24.14229104472")
  }

  class RemoteOppijanumerorekisteriService(config: AppConfig) extends OppijanumerorekisteriService with JsonFormats with Logging {
    import org.json4s.jackson.JsonMethods._
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
    private val callerIdHeader = Header("Caller-Id", "omatsivut.omatsivut.backend")
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

    override def fetchAllOids(pOid: String): List[String] = {
        implicit val formats = DefaultFormats
        val timeout = 1000*30L

        val masterRequest: Request = Request(
          uri = uriFromString(OphUrlProperties.url("oppijanumerorekisteri-service.henkilo-master", pOid)),
          headers = Headers(callerIdHeader))

        val masterOid: String = runHttp[Option[String]](masterRequest) {
          case (200, resultString, _) =>
            val f: json4s.JValue = parse(resultString).asInstanceOf[JObject]
            val oid = f \ "oidHenkilo"
            Some(oid.extract[String])
          case (code, responseString, _) =>
            logger.error("Failed to fetch master oid for user oid {}, response was {}, {}", pOid, Integer.toString(code), responseString)
            None
        }.runFor(timeoutInMillis = timeout).getOrElse(pOid)

        val slaveRequest: Request = Request(
          uri = uriFromString(OphUrlProperties.url("oppijanumerorekisteri-service.henkilo-slaves", masterOid)),
          headers = Headers(callerIdHeader))

        val allOids: List[String] = runHttp(slaveRequest) {
          case (200, resultString, _) =>
            val slaveOids: Seq[String] = parse(resultString).extract[List[JObject]]
              .map(obj => {
                val oidObj = obj \ "oidHenkilo"
                oidObj.extract[String]
              })
            List(masterOid) ++ slaveOids
          case (code, responseString, _) =>
            logger.error("Failed to fetch slave OIDs for user oid {}, response was {}, {}", masterOid, Integer.toString(code), responseString)
            List(masterOid)
        }.runFor(timeoutInMillis = timeout)

      return allOids
    }
  }
}
