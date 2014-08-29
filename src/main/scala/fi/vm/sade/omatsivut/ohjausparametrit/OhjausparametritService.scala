package fi.vm.sade.omatsivut.ohjausparametrit

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.{StubbedExternalDeps, AppConfig}
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.http.DefaultHttpClient
import org.json4s.JsonAST.JValue
import fi.vm.sade.omatsivut.hakemus.domain.Tulokset
import scala.collection.mutable


trait OhjausparametritService {
  def valintatulokset(asId: String): Option[Tulokset]
}

private object OhjausparametritParser extends JsonFormats {
  def parseValintatulokset(json: JValue) = {
    for {
      obj <- (json \ "PH_VTJH").toOption
      start <- (obj \ "dateStart").extractOpt[Long]
      end <- (obj \ "dateEnd").extractOpt[Long]
    } yield Tulokset(start, end)
  }
}

object OhjausparametritService {
  private val cache = mutable.Map.empty[String, Tulokset]

  def apply(implicit appConfig: AppConfig): OhjausparametritService = appConfig match {
    case _ : StubbedExternalDeps => StubbedOhjausparametritService()
    case _ => new RemoteOhjausparametritService(cache)
  }
}

case class StubbedOhjausparametritService() extends OhjausparametritService with JsonFormats {
  def valintatulokset(asId: String) = {
    JsonFixtureMaps.findByKey[JValue]("/mockdata/ohjausparametrit.json", asId).flatMap(OhjausparametritParser.parseValintatulokset(_))
  }
}

class RemoteOhjausparametritService(cache: mutable.Map[String, Tulokset])(implicit appConfig: AppConfig) extends OhjausparametritService with JsonFormats {
  import org.json4s.jackson.JsonMethods._

  def valintatulokset(asId: String) = {
    def storeIntoCache(result: Option[Tulokset]) = {
      result.map { value =>
        cache + (asId -> value)
      }
      result
    }
    def httpGet: (Int, Map[String, List[String]], String) = {
      DefaultHttpClient.httpGet(appConfig.settings.ohjausparametritUrl + "/" + asId).responseWithHeaders()
    }
    def updateCache: Option[Tulokset] = {
      val (responseCode, _, resultString) = httpGet
      responseCode match {
        case 200 => storeIntoCache(parseTulokset(resultString))
        case _ => None
      }
    }
    val cacheHit = cache.get(asId)
    cacheHit match {
      case Some(x) => Some(x)
      case _ => updateCache
    }
  }

  private def parseTulokset(resultString: String): Option[Tulokset] = {
    parse(resultString).extractOpt[JValue].flatMap(OhjausparametritParser.parseValintatulokset(_))
  }
}