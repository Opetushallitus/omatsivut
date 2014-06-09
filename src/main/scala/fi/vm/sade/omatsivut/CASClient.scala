package fi.vm.sade.omatsivut;

import java.util.logging.Logger
import fi.vm.sade.omatsivut.http.HttpClient

object CASClient extends HttpClient {
  
  val settings = AppConfig.loadSettings
  val log = Logger.getLogger(getClass().getSimpleName())

  private def getTicketGrantingTicket(username: String, password: String): Option[String] = {
    val (responseCode, headersMap, resultString) = httpPost(settings.casTicketUrl)
  		.param("username", username)
  		.param("password", password)
  		.responseWithHeaders
    
    responseCode match {
      case 201 => {
        val ticketPattern = """.*/([^/]+)""".r
        headersMap.getOrElse("Location","no location header") match {
          case ticketPattern(value) :: nil => {
            Some(value)
          }
          case location => {
	        log.warning("Successful ticket granting request, but no ticket found! Location header: " + location)
	        None
          }
        }
      }
      case _ => {
        log.warning("Invalid response code (" + responseCode + ") from CAS server!")
        None
      }
    }
  }
  
  def getServiceTicket(appTicketConsumerUrl: String, username: String, password: String): Option[String] = {
    getTicketGrantingTicket(username, password) match {
      case Some(ticket) => {
    	  Some(httpPost(settings.casTicketUrl + "/" + ticket)
    		.param("service", appTicketConsumerUrl)
    		.response)        
      } 
      case None => None
    }
  }
}
