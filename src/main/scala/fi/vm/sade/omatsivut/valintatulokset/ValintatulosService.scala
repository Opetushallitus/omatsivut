package fi.vm.sade.omatsivut.valintatulokset

import fi.vm.sade.omatsivut.config.AppConfig.{StubbedExternalDeps, ITWithSijoitteluService, AppConfig}
import fi.vm.sade.omatsivut.http.{HttpRequest, DefaultHttpClient}
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.security.CASClient
import fi.vm.sade.omatsivut.util.Logging
import org.json4s.JsonAST.JValue
import org.json4s.jackson.JsonMethods._

trait ValintatulosServiceComponent {
  val valintatulosService: ValintatulosService

  class NoOpValintatulosService extends ValintatulosService {
    override def getValintatulos(hakemusOid: String, hakuOid: String) = None
  }

  class MockValintatulosService() extends ValintatulosService with JsonFormats {
    import org.json4s.jackson.JsonMethods._
    val json = """{"hakemusOid":"1.2.246.562.11.00000441369","hakutoiveet":[{"hakukohdeOid":"1.2.246.562.5.72607738902","tarjoajaOid":"1.2.246.562.10.591352080610","tila":"HYVAKSYTTY","vastaanottotieto":"ILMOITETTU","ilmoittautumisTila":null,"jonosija":1,"varasijanNumero":null}]}"""

    override def getValintatulos(hakemusOid: String, hakuOid: String) = {
      None // TODO Better fixture so that front-end tests can be made
      /*if (hakemusOid == "1.2.246.562.11.00000441369")
        Some(parse(json).extract[Valintatulos])
      else
        None*/
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
}

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
}

case class Valintatulos(hakemusOid: String, hakutoiveet: List[HakutoiveenValintatulos])

case class HakutoiveenValintatulos(hakukohdeOid: String,
                                   tarjoajaOid: String,
                                   tila: String,
                                   vastaanottotieto: String,
                                   ilmoittautumisTila: String,
                                   jonosija: Option[Int],
                                   varasijaNumero: Option[Int])
