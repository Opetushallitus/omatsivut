package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.{RemoteApplicationConfig, AppConfig}
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.util.Logging

case class CASClient(val httpClient: HttpClient)(implicit val appConfig: AppConfig) extends Logging {
  
  protected[security] def getTicketGrantingTicket(username: String, password: String): Option[String] = {
    val (responseCode, headersMap, resultString) = httpClient.httpPost(appConfig.settings.casTicketUrl, None)
  		.param("username", username)
  		.param("password", password)
  		.responseWithHeaders
    
    responseCode match {
      case 201 => {
        val ticketPattern = """.*/([^/]+)""".r
        val headerValue = headersMap.getOrElse("Location",List("no location header")).head
        ticketPattern.findFirstMatchIn(headerValue) match {
          case Some(matched) => Some(matched.group(1))
          case None => {
              logger.warn("Successful ticket granting request, but no ticket found! Location header: " + headerValue)
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
        httpClient.httpPost(appConfig.settings.casTicketUrl + "/" + ticket, None)
    		.param("service", appTicketConsumerUrl)
    		.response        
      } 
      case None => None
    }
  }
}
