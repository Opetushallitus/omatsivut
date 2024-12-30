package fi.vm.sade.hakemuseditori.oppijanumerorekisteri

import java.util.concurrent.TimeUnit
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.util.Logging
import org.asynchttpclient.RequestBuilder
import org.http4s._
import org.json4s
import org.json4s.{DefaultFormats, Extraction}

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s.jackson.JsonMethods._

import scala.concurrent.duration.Duration

case class Henkilo(oid: String, hetu: Option[String], oppijanumero: Option[String])

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

  class RemoteOppijanumerorekisteriService(config: AppConfig) extends OppijanumerorekisteriService with JsonFormats with Logging {

    val casConfig: CasConfig = new CasConfig.CasConfigBuilder(
      config.settings.securitySettings.casVirkailijaUsername,
      config.settings.securitySettings.casVirkailijaPassword,
      OphUrlProperties.url("cas.url"),
      OphUrlProperties.url("url-oppijanumerorekisteri-service"),
      AppConfig.callerId,
      AppConfig.callerId,
    "/j_spring_cas_security_check")
    .setJsessionName("JSESSIONID").build

    val casClient: CasClient = CasClientBuilder.build(casConfig)

    implicit private val formats = DefaultFormats

    override def henkilo(personOid: String): Henkilo = {
      val oppijanumerorekisteriUrl = OphUrlProperties.url("oppijanumerorekisteri-service.henkiloByOid", personOid)

      val request = new RequestBuilder().setMethod("GET").setUrl(oppijanumerorekisteriUrl).build
      val future = Future {
        casClient.executeBlocking(request)
      }
      val result = future.map {
        case r if r.getStatusCode == 200 =>
          parse(r.getResponseBodyAsStream()).extract[Henkilo]
        case r =>
          throw new RuntimeException(new RuntimeException(s"Failed to get henkilö for $personOid: ${r.toString()}"))
      }
      Await.result(result, Duration(10, TimeUnit.SECONDS))
    }

    private def uriFromString(url: String): Uri = {
      Uri.fromString(url).toOption.get
    }

    override def fetchAllDuplicateOids(oppijanumero: String): Set[String] = {
      logger.debug(s"Fetching duplicate oids for oppijanumero $oppijanumero")
      val timeout = Duration(30, TimeUnit.SECONDS)

      val url = OphUrlProperties.url("oppijanumerorekisteri-service.duplicatesByPersonOids")
      val body: json4s.JValue = Extraction.decompose(Map("henkiloOids" -> List(oppijanumero)))
      val bodyString = compact(render(body))
      val request = new RequestBuilder()
        .setMethod("POST")
        .setUrl(url)
        .addHeader("Content-type", "application/json")
        .addHeader("Accept", "application/json")
        .setBody(bodyString).build
      val future = Future {
        casClient.executeBlocking(request)
      }
      val result = future.map {
        case r if r.getStatusCode == 200 =>
          parse(r.getResponseBodyAsStream()).extract[Seq[Henkiloviite]]
        case r =>
          logger.error(s"Failed to fetch henkiloviite data for user oid $oppijanumero, response was ${r.getStatusCode}, ${r.getResponseBody}")
          throw new RuntimeException(s"Failed to fetch henkiloviite data for user oid $oppijanumero: $r")
      }
      val henkiloviitteet = Await.result(result, timeout)
      val allHenkiloOids: Set[String] = henkiloviitteet.flatMap(viite => Set(viite.henkiloOid, viite.masterOid)).++(Seq(oppijanumero)).toSet
      if (allHenkiloOids.size > 1) {
        logger.info(s"Got ${allHenkiloOids.size} in total for oppijanumero $oppijanumero : $allHenkiloOids")
      }
      allHenkiloOids
    }
  }
}

case class Henkiloviite(henkiloOid: String, masterOid: String)
