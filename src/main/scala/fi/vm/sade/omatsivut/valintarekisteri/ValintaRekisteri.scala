package fi.vm.sade.omatsivut.valintarekisteri

import org.json4s.jackson.Serialization
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.utils.http.{HttpClient, DefaultHttpClient}
import fi.vm.sade.utils.slf4j.Logging

import scala.language.implicitConversions

trait ValintaRekisteriComponent {
  val valintaRekisteriService: ValintaRekisteriService
}

trait ValintaRekisteriService {
  def isEnabled: Boolean
  def vastaanota(henkilo: String, hakukohde: String, ilmoittaja: String): Boolean
}

case class VastaanottoIlmoitus(henkilo: String, hakukohde: String, ilmoittaja: String)

class RemoteValintaRekisteriService(valintaRekisteriServiceUrl: String, client: HttpClient = DefaultHttpClient) extends ValintaRekisteriService with JsonFormats with Logging {
  override def vastaanota(henkilo: String, hakukohde: String, ilmoittaja: String): Boolean = {
    client.httpPost(valintaRekisteriServiceUrl, Some(Serialization.write(VastaanottoIlmoitus(henkilo, hakukohde, ilmoittaja))))
      .header("Content-type", "application/json")
      .header("Caller-Id", "omatsivut.omatsivut.backend")
      .header("Palvelukutsu.Lahettaja.JarjestelmaTunnus", ilmoittaja)
      .responseWithHeaders() match {
        case (200, headers, result) =>
          logger.debug(s"POST $valintaRekisteriServiceUrl: headers $headers, body $result")
          true
        case (403, headers, result) =>
          logger.debug(s"acceptance blocked by prior: $result")
          false
        case (code, headers, result) =>
          logger.error(s"Response code $code from valintarekisteri for $valintaRekisteriServiceUrl")
          throw RemoteServiceException(result)
      }
  }

  override def isEnabled = valintaRekisteriServiceUrl.length > 0
}

case class RemoteServiceException(message: String) extends Exception(message)

class MockedValintaRekisteriServiceForIT extends ValintaRekisteriService with JsonFormats with Logging {
  override def vastaanota(henkilo: String, hakukohde: String, ilmoittaja: String): Boolean = {
    throw new RuntimeException("MockedValintaRekisteriService is not in use")
  }

  override def isEnabled = false
}