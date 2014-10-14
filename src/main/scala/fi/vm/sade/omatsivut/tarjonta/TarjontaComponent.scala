package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.config.RemoteApplicationConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.http.DefaultHttpClient
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.omatsivut.security.CasTicketRequiring
import org.json4s.JsonAST.JValue

trait TarjontaComponent {
  this: OhjausparametritComponent =>

  class StubbedTarjontaService extends TarjontaService with JsonFormats {
    override def haku(oid: String)(implicit lang: Language.Language) = {
      val haku = JsonFixtureMaps.findByKey[JValue]("/mockdata/haut.json", oid).flatMap(HakuParser.parseHaku(_)).map {h => Haku(h)}
      haku.map {h =>
        val tulokset = ohjausparametritService.valintatulokset(oid)
        h.copy(results = tulokset)
      }
    }
  }

  class RemoteTarjontaService(val config: RemoteApplicationConfig, val casTicketUrl: String) extends TarjontaService with JsonFormats with CasTicketRequiring {
    import org.json4s.jackson.JsonMethods._

    override def haku(oid: String)(implicit lang: Language.Language) : Option[Haku] = {
      withServiceTicket(serviceTicket => {
        val (responseCode, _, resultString) =
          DefaultHttpClient.httpGet("https://itest-virkailija.oph.ware.fi/tarjonta-service/rest/v1/haku/" + oid)
            .param("ticket", serviceTicket)
            .responseWithHeaders()
        responseCode match {
          case 200 =>
            parse(resultString).extractOpt[JValue].flatMap(HakuParser.parseHaku(_)).map { tarjontaHaku =>
              val tulokset = ohjausparametritService.valintatulokset(oid)
              Haku(tarjontaHaku).copy(results = tulokset)
            }
        }
      })
    }
  }
}

case class TarjontaHaku(oid: String, hakuaikas: List[TarjontaHakuaika], hakutapaUri: String, hakutyyppiUri: String, kohdejoukkoUri: String, usePriority: Boolean, nimi: Map[String, String])
case class TarjontaHakuaika(hakuaikaId: String, alkuPvm: Long, loppuPvm: Long)

private object HakuParser extends JsonFormats {
  def parseHaku(json: JValue) = {
    for {
      obj <- (json \ "result").toOption
      h <- obj.extractOpt[TarjontaHaku]
    } yield h
  }
}

trait TarjontaService {
  def haku(oid: String)(implicit lang: Language.Language) : Option[Haku]
}