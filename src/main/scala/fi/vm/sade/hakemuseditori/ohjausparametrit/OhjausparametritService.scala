package fi.vm.sade.hakemuseditori.ohjausparametrit

import fi.vm.sade.hakemuseditori.fixtures.JsonFixtureMaps
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.memoize.TTLOptionalMemoize
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.{HaunAikataulu, HaunParametrit, TulostenJulkistus}
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonAST.JValue


trait OhjausparametritComponent {
  val ohjausparametritService: OhjausparametritService

  class StubbedOhjausparametritService extends OhjausparametritService with JsonFormats {
    def haunAikataulu(asId: String) = {
      JsonFixtureMaps.findByKey[JValue]("/hakemuseditorimockdata/ohjausparametrit.json", asId).flatMap(OhjausparametritParser.parseHaunParametrit(_).flatMap(_.haunAikataulu))
    }
    def haunParametrit(asId: String) = {
      JsonFixtureMaps.findByKey[JValue]("/hakemuseditorimockdata/ohjausparametrit.json", asId).flatMap(OhjausparametritParser.parseHaunParametrit(_))
    }
  }

  object CachedRemoteOhjausparametritService {
    def apply(ohjausparametritUrl: String): OhjausparametritService = {
      val service = new RemoteOhjausparametritService(ohjausparametritUrl)
      val haunParametritMemo = TTLOptionalMemoize.memoize(service.haunParametrit _, "ohjausparametrit haun aikataulu", 60 * 60, 256)

      new OhjausparametritService() {
        override def haunAikataulu(asId: String) = haunParametritMemo(asId).flatMap(_.haunAikataulu)
        override def haunParametrit(asId: String) = haunParametritMemo(asId)
      }

    }
  }

  class RemoteOhjausparametritService(ohjausparametritUrl: String) extends OhjausparametritService with JsonFormats with Logging {
    import org.json4s.jackson.JsonMethods._

    def haunAikataulu(asId: String) = {
      haunParametrit(asId).flatMap(_.haunAikataulu)
    }
    def haunParametrit(asId: String) = {
      val url = ohjausparametritUrl + "/" + asId
      val (responseCode, _, resultString) = DefaultHttpClient.httpGet(url)(AppConfig.callerId)
        .responseWithHeaders

      responseCode match {
        case 200 =>
          parse(resultString, useBigDecimalForDouble = false).extractOpt[JValue].flatMap(OhjausparametritParser.parseHaunParametrit(_))
        case errorCode =>
          logger.error(s"Response code ${errorCode} from ohjausparametrit-service at ${url}, expected: 200. Content: ${resultString}")
          None
      }
    }
  }

  private object OhjausparametritParser extends JsonFormats {
    def parseHaunParametrit(json: JValue) = {
      val julkistus = for {
        obj <- (json \ "PH_VTJH").toOption
        start <- (obj \ "dateStart").extractOpt[Long]
        end <- (obj \ "dateEnd").extractOpt[Long]
      } yield TulostenJulkistus(start, end)
      val hakukierrosPaattyy = for {
        obj <- (json \ "PH_HKP").toOption
        end <- (obj \ "date").extractOpt[Long]
      } yield end
      val jarjestetytHakutoiveet = for {
        param <- (json \ "jarjestetytHakutoiveet").extractOpt[Boolean]
      } yield param
      Some(HaunParametrit(haunAikataulu = Some(HaunAikataulu(julkistus, hakukierrosPaattyy)), jarjestetytHakutoiveet = jarjestetytHakutoiveet))
    }
  }
}

trait OhjausparametritService {
  def haunAikataulu(asId: String): Option[HaunAikataulu]
  def haunParametrit(asId: String): Option[HaunParametrit]
}

