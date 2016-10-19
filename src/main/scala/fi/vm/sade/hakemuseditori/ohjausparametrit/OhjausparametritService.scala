package fi.vm.sade.hakemuseditori.ohjausparametrit

import fi.vm.sade.hakemuseditori.fixtures.JsonFixtureMaps
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.memoize.TTLOptionalMemoize
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.{HaunAikataulu, TulostenJulkistus}
import org.json4s.JsonAST.JValue


trait OhjausparametritComponent {
  val ohjausparametritService: OhjausparametritService

  class StubbedOhjausparametritService extends OhjausparametritService with JsonFormats {
    def haunAikataulu(asId: String) = {
      JsonFixtureMaps.findByKey[JValue]("/hakemuseditorimockdata/ohjausparametrit.json", asId).flatMap(OhjausparametritParser.parseHaunAikataulu(_))
    }
  }

  object CachedRemoteOhjausparametritService {
    def apply(ohjausparametritUrl: String): OhjausparametritService = {
      val service = new RemoteOhjausparametritService(ohjausparametritUrl)
      val haunAikatauluMemo = TTLOptionalMemoize.memoize(service.haunAikataulu _, "ohjausparametrit haun aikataulu", 60 * 60, 256)

      new OhjausparametritService() {
        override def haunAikataulu(asId: String) = haunAikatauluMemo(asId)
      }
    }
  }

  class RemoteOhjausparametritService(ohjausparametritUrl: String) extends OhjausparametritService with JsonFormats {
    import org.json4s.jackson.JsonMethods._

    def haunAikataulu(asId: String) = {
      val (responseCode, _, resultString) = DefaultHttpClient.httpGet(ohjausparametritUrl + "/" + asId)
        .responseWithHeaders

      responseCode match {
        case 200 =>
          parse(resultString).extractOpt[JValue].flatMap(OhjausparametritParser.parseHaunAikataulu(_))
        case _ => None
      }
    }
  }

  private object OhjausparametritParser extends JsonFormats {
    def parseHaunAikataulu(json: JValue) = {
      val julkistus = for {
        obj <- (json \ "PH_VTJH").toOption
        start <- (obj \ "dateStart").extractOpt[Long]
        end <- (obj \ "dateEnd").extractOpt[Long]
      } yield TulostenJulkistus(start, end)
      val hakukierrosPaattyy = for {
        obj <- (json \ "PH_HKP").toOption
        end <- (obj \ "date").extractOpt[Long]
      } yield end
      Some(HaunAikataulu(julkistus, hakukierrosPaattyy))
    }
  }
}

trait OhjausparametritService {
  def haunAikataulu(asId: String): Option[HaunAikataulu]
}

