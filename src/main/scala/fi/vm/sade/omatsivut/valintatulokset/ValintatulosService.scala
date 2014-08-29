package fi.vm.sade.omatsivut.valintatulokset

import fi.vm.sade.omatsivut.AppConfig.{AppConfig, StubbedExternalDeps}
import fi.vm.sade.omatsivut.http.DefaultHttpClient
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.security.CASClient
import org.json4s.JsonAST.JValue

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
}

object ValintatulosService {
  def apply(implicit appConfig: AppConfig): ValintatulosService = appConfig match {
    case x: StubbedExternalDeps => MockValintatulosService()
    case _ => RemoteValintatulosService()
  }
}

case class MockValintatulosService() extends ValintatulosService with JsonFormats {
  import org.json4s.jackson.JsonMethods._
  val json = """{"hakemusOid":"1.2.246.562.11.00000878229","hakutoiveet":[{"hakukohdeOid":"1.2.246.562.5.72607738902","tarjoajaOid":"1.2.246.562.10.591352080610","tila":"HYVAKSYTTY","vastaanottotieto":"ILMOITETTU","ilmoittautumisTila":null,"jonosija":1,"varasijanNumero":null}]}"""

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    Some(parse(json).extract[Valintatulos])
  }
}

case class RemoteValintatulosService(implicit appConfig: AppConfig) extends ValintatulosService with JsonFormats {
  import org.json4s.jackson.JsonMethods._

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    val t  = CASClient(DefaultHttpClient).getServiceTicket(appConfig.settings.authenticationServiceConfig)
    CASClient(DefaultHttpClient).getServiceTicket(appConfig.settings.sijoitteluServiceConfig).flatMap { ticket =>
      val (responseCode, _, resultString) = DefaultHttpClient.httpGet(appConfig.settings.sijoitteluServiceConfig.url + "/resources/sijoittelu/1.2.246.562.29.92478804245/sijoitteluajo/latest/hakemus/yhteenveto/1.2.246.562.11.00000878229")
        .param("ticket", ticket)
        .responseWithHeaders

      responseCode match {
        case 200 =>
          parse(resultString).extractOpt[JValue].map(_.extract[Valintatulos])
        case _ => None
      }
    }
  }
}

case class Valintatulos(
                                             hakemusOid: String,
                                             hakutoiveet: List[HakutoiveenValintatulos])

case class HakutoiveenValintatulos(
                                              hakukohdeOid: String,
                                              tarjoajaOid: String,
                                              tila: String,
                                              vastaanottotieto: String,
                                              ilmoittautumisTila: String,
                                              jonosija: Option[Int],
                                              varasijaNumero: Option[Int])