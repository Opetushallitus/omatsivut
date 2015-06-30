package fi.vm.sade.omatsivut.valintarekisteri

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Vastaanotto
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.utils.slf4j.Logging

trait ValintaRekisteriComponent {
  val valintaRekisteriService: ValintaRekisteriService
}

trait ValintaRekisteriService {
  def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto): Boolean
}

case class VastaanottoIlmoitus(henkilo: String, hakukohde: String, ilmoittaja: String)

class RemoteValintaRekisteriService(valintaRekisteriServiceUrl: String) extends ValintaRekisteriService with JsonFormats with Logging {
  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto): Boolean = {
    import org.json4s.jackson.Serialization
    if (valintaRekisteriServiceUrl.length > 0) {
      val url = valintaRekisteriServiceUrl + "/vastaanotto"
      val request = DefaultHttpClient.httpPost(url, Some(Serialization.write(new VastaanottoIlmoitus(vastaanotto.muokkaaja, vastaanotto.hakukohdeOid, vastaanotto.muokkaaja))))
        .header("Content-type", "application/json")
        .header("Caller-Id", "omatsivut.omatsivut.backend")
        .header("Palvelukutsu.Lahettaja.JarjestelmaTunnus", vastaanotto.muokkaaja)
      request.responseWithHeaders match {
        case (200, _, resultString) => {
          logger.info("POST " + url + ": " + resultString)
          true
        }
        case (errorCode, _, resultString) =>
          logger.error("Response code " + errorCode + " from valinta-rekisteri at " + url)
          false
      }
    } else {
      true
    }
  }
}