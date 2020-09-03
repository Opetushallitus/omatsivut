package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.utils.cas.CasClient.{OppijaAttributes, ServiceTicket, SessionCookie, Username}
import fi.vm.sade.utils.cas.{CasClient, CasParams}
import org.http4s._
import org.http4s.client._
import org.http4s.dsl._
import scalaz.concurrent.Task



class FakeCasClient(casBaseUrl: String, client: Client, callerId: String) extends CasClient(casBaseUrl, client, callerId) with OmatsivutDbTools {


  override def validateServiceTicket[R](service: String)(serviceTicket: ServiceTicket, responseHandler: Response => Task[R]): Task[R] =
    responseHandler(Ok("Tämä olis OK-ticketvalidointivastaus").unsafePerformSync)

  override def fetchCasSession(params: CasParams, sessionCookieName: String): Task[SessionCookie] = {
    logger.debug("fetsataan!")

    Task.now("keksi")
  }

  override def decodeOppijaAttributes: Response => Task[OppijaAttributes] = response => Task.now(


    Map("nationalIdentificationNumber" -> TestFixture.testHetu,
      "personOid" -> TestFixture.personOid,
      "displayName" -> TestFixture.displayName))

  override def decodeVirkailijaUsername: Response => Task[Username] = response => Task.now("frank-virkailija")

}
