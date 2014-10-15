package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.RemoteApplicationConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.http.DefaultHttpClient
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.memoize.TTLOptionalMemoize
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.omatsivut.security.CasTicketRequiring
import org.json4s.JsonAST.JValue

trait TarjontaComponent {
  this: OhjausparametritComponent =>

  class StubbedTarjontaService extends TarjontaService with JsonFormats {
    override def haku(oid: String, lang: Language.Language) = {
      val haku = JsonFixtureMaps.findByKey[JValue]("/mockdata/haut.json", oid).flatMap(HakuParser.parseHaku(_)).map {h => Haku(h, lang)}
      haku.map {h =>
        val tulokset = ohjausparametritService.valintatulokset(oid)
        h.copy(results = tulokset)
      }
    }
  }

  object CachedRemoteTarjontaService {
    def apply(implicit appConfig: AppConfig): TarjontaService = {
      val service = new RemoteTarjontaService()
      val tarjontaMemo = TTLOptionalMemoize.memoize(service.haku _, 60 * 60)

      new TarjontaService {
        override def haku(oid: String, lang: Language): Option[Haku] = tarjontaMemo(oid, lang)
      }
    }
  }

  class RemoteTarjontaService(implicit appConfig: AppConfig) extends TarjontaService with JsonFormats {
    import org.json4s.jackson.JsonMethods._

    override def haku(oid: String, lang: Language.Language) : Option[Haku] = {
      val (responseCode, _, resultString) =
        DefaultHttpClient.httpGet(appConfig.settings.tarjontaUrl + "/haku/" + oid)
          .responseWithHeaders()
      responseCode match {
        case 200 =>
          parse(resultString).extractOpt[JValue].flatMap(HakuParser.parseHaku(_)).map { tarjontaHaku =>
            val tulokset = ohjausparametritService.valintatulokset(oid)
            Haku(tarjontaHaku, lang).copy(results = tulokset)
          }
      }
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
  def haku(oid: String, lang: Language.Language) : Option[Haku]
}