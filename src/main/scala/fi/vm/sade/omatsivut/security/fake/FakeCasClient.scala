package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import fi.vm.sade.utils.cas.CasClient.{OppijaAttributes, ServiceTicket, SessionCookie, Username}
import fi.vm.sade.utils.cas.{CasClient, CasParams}
import org.http4s._
import org.http4s.client._
import org.http4s.dsl._
import org.scalatra.NotFound
import scalaz.concurrent.Task

class FakeCasClient(casBaseUrl: String, client: Client, callerId: String, authenticationInfoService: AuthenticationInfoService) extends CasClient(casBaseUrl, client, callerId) {
  override def validateServiceTicket[R](service: String)(serviceTicket: ServiceTicket, responseHandler: Response => Task[R]): Task[R] =
    responseHandler(Ok("Tämä olis OK-ticketvalidointivastaus").unsafePerformSync)

  override def fetchCasSession(params: CasParams, sessionCookieName: String): Task[SessionCookie] = {
    //logger.debug(params)

    Task.now("keksi")
  }

  def decodeOppijaAttributes(hetu: String): Response => Task[OppijaAttributes] = response => Task.now {

    val oidPerson = authenticationInfoService.getOnrHenkilo(hetu)
    oidPerson match {
      case None => {
        throw new RuntimeException
        Map()
      }
      case Some(x) => {

        Map("nationalIdentificationNumber" -> oidPerson.get.hetu,
          "personOid" -> oidPerson.get.oidHenkilo,
          "displayName" -> (oidPerson.get.kutsumanimi + " " + oidPerson.get.sukunimi))
      }

    }
  }

  override def decodeVirkailijaUsername: Response => Task[Username] = response => Task.now("frank-virkailija")
}


