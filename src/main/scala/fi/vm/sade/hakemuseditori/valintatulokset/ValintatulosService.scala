package fi.vm.sade.hakemuseditori.valintatulokset

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Valintatulos
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.Timer.timed
import fi.vm.sade.hakemuseditori.valintatulokset.domain.{VastaanottoAction}
import fi.vm.sade.omatsivut.OphUrlProperties
import org.json4s.JsonAST.JValue

class ValintatulosException extends Exception

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
  def vastaanota(henkiloOid: String, hakemusOid: String, hakukohdeOid: String, vastaanotto: VastaanottoAction): Boolean
}


trait ValintatulosServiceComponent {
  val valintatulosService: ValintatulosService
}

class NoOpValintatulosService extends ValintatulosService {
  override def getValintatulos(hakemusOid: String, hakuOid: String) = None

  override def vastaanota(henkiloOid: String, hakemusOid: String, hakukohdeOid: String, vastaanotto: VastaanottoAction) = true
}

class FailingRemoteValintatulosService() extends RemoteValintatulosService {
  var shouldFail = false

  override def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos] = {
    if(shouldFail) throw new ValintatulosException()
    super.getValintatulos(hakemusOid, hakuOid)
  }
}

class RemoteValintatulosService extends ValintatulosService with JsonFormats with Logging {
  import org.json4s.jackson.JsonMethods._

  def applyFixture(fixture: String) {
    applyFixtureWithQuery( Map("fixturename"->fixture))
  }

  def applyFixtureWithQuery(query: Map[String,AnyRef]) {
    val url = OphUrlProperties.url("valinta-tulos-service.fixtures.apply", query)
    DefaultHttpClient.httpPut(url).responseWithHeaders match {
      case (200, _, resultString) =>
        logger.info("Using valinta-tulos-service fixture: " + query)
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " applying fixtures at " + url)
    }
  }


  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    val url = OphUrlProperties.url("valinta-tulos-service.valintatulos", hakuOid, hakemusOid)
    val request = DefaultHttpClient.httpGet(url)

    timed("ValintatulosService get", 1000){request.responseWithHeaders} match {
      case (200, _, resultString) => {
        try {
          parse(resultString).extractOpt[JValue].map(_.extract[Valintatulos])
        } catch {
          case e:Exception => {
            logger.error("Error processing response from valinta-tulos-service at " + url + ", response was " + resultString, e)
            None
          }
        }
      }
      case (404, _, _) =>
        None
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " fetching data from valinta-tulos-service at " + url)
        throw new ValintatulosException()
    }
  }

  override def vastaanota(henkiloOid: String, hakemusOid: String, hakukohdeOid: String, vastaanotto: VastaanottoAction) = {
    import org.json4s.jackson.Serialization

    val url = OphUrlProperties.url("valinta-tulos-service.vastaanota", henkiloOid, hakemusOid, hakukohdeOid)
    val request = DefaultHttpClient.httpPost(url, Some(Serialization.write(vastaanotto))).header("Content-type", "application/json")
    request.responseWithHeaders match {
      case (200, _, resultString) => {
        logger.debug("POST " + url + ": " + resultString)
        true
      }
      case (403, headers, result) =>
        logger.debug(s"acceptance blocked by prior: $result")
        false
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " from valinta-tulos-service at " + url)
        throw RemoteServiceException(resultString)
    }
  }
}


case class RemoteServiceException(message: String) extends Exception(message)
