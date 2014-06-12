package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.http.HttpClient
import org.json4s.{DefaultFormats, Formats}
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s._

object AuthenticationInfoService extends Logging with HttpClient {
  
  val settings = AppConfig.loadSettings 
  
  implicit val formats = DefaultFormats
  
  def getHenkiloOID(hetu : String)  = {
      val ticket = CASClient.getServiceTicket(settings.authenticationService.url + "/" + settings.authenticationService.ticketConsumerPath , settings.authenticationService.username, settings.authenticationService.password)

      val (responseCode, headersMap, resultString) = httpGet(settings.authenticationService.url + "/" + settings.authenticationService.path + "/" + hetu)
        .param("ticket", ticket.getOrElse("no_ticket"))
        .responseWithHeaders
      
      responseCode match {
        case 404 => None
        case _ => {
          val json = parse(resultString)
          logger.info("Got user info: " + json)
          (for {
             JObject(child) <- json
             JField("oidHenkilo", JString(oid))  <- child
          } yield oid).headOption
        }
      }
  }


}