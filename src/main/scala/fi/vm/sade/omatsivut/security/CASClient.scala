package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.AppConfig
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.{AppConfig, Logging, RemoteApplicationConfig}

case class CASClient(implicit val appConfig: AppConfig) extends Logging {
  
  private def getTicketGrantingTicket(username: String, password: String): Option[String] = {
    val (responseCode, headersMap, resultString) = HttpClient.httpPost(appConfig.settings.casTicketUrl)
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
	        logger.warn("Successful ticket granting request, but no ticket found! Location header: " + location)
	        None
          }
        }
      }
      case _ => {
        logger.warn("Invalid response code (" + responseCode + ") from CAS server!")
        None
      }
    }
  }

  def getServiceTicket(service: RemoteApplicationConfig): Option[String] = {
    getServiceTicket(service.url + "/" + service.ticketConsumerPath , service.username, service.password)
  }
  
  private def getServiceTicket(appTicketConsumerUrl: String, username: String, password: String): Option[String] = {
    getTicketGrantingTicket(username, password) match {
      case Some(ticket) => {
        HttpClient.httpPost(appConfig.settings.casTicketUrl + "/" + ticket)
    		.param("service", appTicketConsumerUrl)
    		.response        
      } 
      case None => None
    }
  }
}
