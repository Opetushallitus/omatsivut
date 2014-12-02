package fi.vm.sade.omatsivut.valintatulokset

import fi.vm.sade.omatsivut.http.DefaultHttpClient
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.util.Logging
import fi.vm.sade.omatsivut.util.Timer.timed
import fi.vm.sade.omatsivut.valintatulokset.domain.{Valintatulos, Vastaanotto}
import org.json4s.JsonAST.JValue

class ValintatulosException extends Exception

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
  def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto): Boolean
}


trait ValintatulosServiceComponent {
  val valintatulosService: ValintatulosService
}

class NoOpValintatulosService extends ValintatulosService {
  override def getValintatulos(hakemusOid: String, hakuOid: String) = None

  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) = true
}

class FailingRemoteValintatulosService(valintatulosServiceUrl: String) extends RemoteValintatulosService(valintatulosServiceUrl) {
  var shouldFail = false

  override def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos] = {
    if(shouldFail) throw new ValintatulosException()
    super.getValintatulos(hakemusOid, hakuOid)
  }
}

class RemoteValintatulosService(valintatulosServiceUrl: String) extends ValintatulosService with JsonFormats with Logging {
  import org.json4s.jackson.JsonMethods._

  def applyFixture(fixture: String) {
    applyFixtureWithQuery("fixturename="+fixture)
  }

  def applyFixtureWithQuery(query: String) {
    val url = valintatulosServiceUrl + "/util/fixtures/apply?" + query
    DefaultHttpClient.httpPut(url).responseWithHeaders match {
      case (200, _, resultString) =>
        logger.info("Using valinta-tulos-service fixture: " + query)
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " applying fixtures at " + url)
    }
  }


  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    val url = valintatulosServiceUrl + "/haku/"+hakuOid+"/hakemus/"+hakemusOid
    val request = DefaultHttpClient.httpGet(url)

    timed(1000, "ValintatulosService get"){request.responseWithHeaders} match {
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

  override def vastaanota(hakemusOid: String, hakuOid: String, vastaanotto: Vastaanotto) = {
    import org.json4s.jackson.Serialization
    val url = valintatulosServiceUrl + "/haku/"+hakuOid+"/hakemus/"+hakemusOid+"/vastaanota"
    val request = DefaultHttpClient.httpPost(url, Some(Serialization.write(vastaanotto))).header("Content-type", "application/json")
    request.responseWithHeaders match {
      case (200, _, resultString) => {
        logger.debug("POST " + valintatulosServiceUrl + ": " + resultString)
        true
      }
      case (errorCode, _, resultString) =>
        logger.error("Response code " + errorCode + " from valinta-tulos-service at " + url)
        false
    }
  }
}