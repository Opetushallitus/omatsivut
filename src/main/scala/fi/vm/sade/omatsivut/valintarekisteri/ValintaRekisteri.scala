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
  def vastaanota(henkilo: String, hakukohde: String, ilmoittaja: String): Boolean
}

case class VastaanottoIlmoitus(henkilo: String, hakukohde: String, ilmoittaja: String)

class RemoteValintaRekisteriService(valintaRekisteriServiceUrl: String, client: HttpClient = DefaultHttpClient) extends ValintaRekisteriService with JsonFormats with Logging {
  override def vastaanota(henkilo: String, hakukohde: String, ilmoittaja: String): Boolean = {
    if (valintaRekisteriServiceUrl.length > 0) {
      val url = s"$valintaRekisteriServiceUrl/vastaanotto"
      client.httpPost(url, Some(Serialization.write(VastaanottoIlmoitus(henkilo, hakukohde, ilmoittaja))))
        .header("Content-type", "application/json")
        .header("Caller-Id", "omatsivut.omatsivut.backend")
        .header("Palvelukutsu.Lahettaja.JarjestelmaTunnus", ilmoittaja)
        .responseWithHeaders() match {
          case (200, headers, result) =>
            logger.debug(s"POST $url: headers $headers, body $result")
            true
          case (code, _, _) =>
            logger.error(s"Response code $code from valintarekisteri for $url")
            false
        }
    } else {
      true
    }
  }
}

class MockedValintaRekisteriService extends ValintaRekisteriService with JsonFormats with Logging {
  override def vastaanota(henkilo: String, hakukohde: String, ilmoittaja: String): Boolean = hakukohde match {
    case "1.2.246.562.5.72607738903" => false
    case _ => true
  }
}