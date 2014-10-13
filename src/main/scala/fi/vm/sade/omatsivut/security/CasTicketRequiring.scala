package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.RemoteApplicationConfig
import fi.vm.sade.omatsivut.http.DefaultHttpClient

trait CasTicketRequiring {
  val casTicketUrl: String
  val config: RemoteApplicationConfig

  def serviceTicket = new CASClient(DefaultHttpClient, casTicketUrl).getServiceTicket(config)
  def withServiceTicket[T](block: String => Option[T]) = serviceTicket match {
    case None => None
    case Some(ticket) => block(ticket)
  }
}
