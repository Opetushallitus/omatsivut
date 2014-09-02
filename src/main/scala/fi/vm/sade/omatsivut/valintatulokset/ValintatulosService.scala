package fi.vm.sade.omatsivut.valintatulokset

import fi.vm.sade.omatsivut.config.AppConfig.{StubbedExternalDeps, ITWithSijoitteluService, AppConfig}
import fi.vm.sade.omatsivut.http.{HttpRequest, DefaultHttpClient}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.security.CASClient
import fi.vm.sade.omatsivut.util.Logging
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
}

object ValintatulosService {
  def apply(implicit appConfig: AppConfig): ValintatulosService = appConfig match {
    case x: ITWithSijoitteluService =>
      new RemoteValintatulosService("http://localhost:8180/resources/sijoittelu") {
        override def makeRequest(url: String) =  {
          super.makeRequest(url).map(_.header("Authorization", "Basic " + System.getProperty("omatsivut.sijoittelu.auth")))
        }
      }
    case x: StubbedExternalDeps =>
      new MockValintatulosService()
    case _ =>
      new NoOpValintatulosService
  }
}

class NoOpValintatulosService extends ValintatulosService {
  override def getValintatulos(hakemusOid: String, hakuOid: String) = None
}

case class MockValintatulosService() extends ValintatulosService with JsonFormats {
  import org.json4s.jackson.JsonMethods._
  val json = """{"hakemusOid":"1.2.246.562.11.00000878229","hakutoiveet":[{"hakukohdeOid":"1.2.246.562.5.72607738902","tarjoajaOid":"1.2.246.562.10.591352080610","tila":"HYVAKSYTTY","vastaanottotieto":"ILMOITETTU","ilmoittautumisTila":null,"jonosija":1,"varasijanNumero":null}]}"""

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    Some(parse(json).extract[Valintatulos])
  }
}

class RemoteValintatulosService(sijoitteluServiceUrl: String) extends ValintatulosService with JsonFormats with Logging {
  import org.json4s.jackson.JsonMethods._

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    val url = sijoitteluServiceUrl + "/"+hakuOid+"/sijoitteluajo/latest/hakemus/yhteenveto/"+hakemusOid
    makeRequest(url).flatMap{ request =>
      request.responseWithHeaders match {
        case (200, _, resultString) =>
          parse(resultString).extractOpt[JValue].map(_.extract[Valintatulos])
        case (errorCode, _, resultString) =>
          logger.error("Response code " + errorCode + " fetching data from sijoittelu-service at " + url)
          None
      }
    }
  }

  def makeRequest(url: String): Option[HttpRequest] = {
    Some(DefaultHttpClient.httpGet(url))
  }
}

class RemoteValintatulosServiceWithCAS(implicit appConfig: AppConfig) extends RemoteValintatulosService(appConfig.settings.sijoitteluServiceConfig.url) with JsonFormats {
  override def makeRequest(url: String) = {
    super.makeRequest(url).flatMap{ request =>
      val t  = CASClient(DefaultHttpClient).getServiceTicket(appConfig.settings.authenticationServiceConfig)
      CASClient(DefaultHttpClient).getServiceTicket(appConfig.settings.sijoitteluServiceConfig).map { ticket =>
        request.param("ticket", ticket)
      }
    }
  }
}

case class Valintatulos(hakemusOid: String, hakutoiveet: List[HakutoiveenValintatulos])

case class HakutoiveenValintatulos(hakukohdeOid: String,
                                   tarjoajaOid: String,
                                   tila: String,
                                   vastaanottotieto: String,
                                   ilmoittautumisTila: String,
                                   jonosija: Option[Int],
                                   varasijaNumero: Option[Int])
