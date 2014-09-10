package fi.vm.sade.omatsivut.valintatulokset

import fi.vm.sade.omatsivut.config.AppConfig.{StubbedExternalDeps, ITWithValintaTulosService, AppConfig}
import fi.vm.sade.omatsivut.http.{HttpRequest, DefaultHttpClient}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.security.CASClient
import fi.vm.sade.omatsivut.util.Logging
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

trait ValintatulosServiceComponent {
  val valintatulosService: ValintatulosService
}

class NoOpValintatulosService extends ValintatulosService {
  override def getValintatulos(hakemusOid: String, hakuOid: String) = None
}

class MockValintatulosService() extends ValintatulosService with JsonFormats {
  private var valintatulokset: List[Valintatulos] = Nil

  def useFixture(fixture: List[Valintatulos]) = {
    valintatulokset = fixture
  }

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    valintatulokset.find(_.hakemusOid == hakemusOid)
  }
}

class RemoteValintatulosService(sijoitteluServiceUrl: String) extends ValintatulosService with JsonFormats with Logging {
  import org.json4s.jackson.JsonMethods._

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    val url = sijoitteluServiceUrl + "/haku/"+hakuOid+"/hakemus/"+hakemusOid
    makeRequest(url).flatMap{ request =>
      request.responseWithHeaders match {
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
        case (errorCode, _, resultString) =>
          logger.error("Response code " + errorCode + " fetching data from valinta-tulos-service at " + url)
          None
      }
    }
  }

  def makeRequest(url: String): Option[HttpRequest] = {
    Some(DefaultHttpClient.httpGet(url))
  }
}

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
}

case class Valintatulos(hakemusOid: String, hakutoiveet: List[HakutoiveenValintatulos])

case class HakutoiveenValintatulos(hakukohdeOid: String,
                                   tarjoajaOid: String,
                                   valintatila: String,
                                   vastaanottotila: Option[String],
                                   ilmoittautumistila: Option[String],
                                   vastaanotettavuustila: String,
                                   jonosija: Option[Int],
                                   varasijanumero: Option[Int])
