package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import org.http4s.client.Client

import cats.effect.IO
import fi.vm.sade.omatsivut.cas.CasClient.{OppijaAttributes, ServiceTicket, SessionCookie, Username}
import fi.vm.sade.omatsivut.cas.{CasClient, CasParams}
import org.http4s.Response

class FakeCasClient(casBaseUrl: String, client: Client[IO], callerId: String, authenticationInfoService: AuthenticationInfoService) extends CasClient(casBaseUrl, client, callerId) {

  override def validateServiceTicketWithOppijaAttributes(service: String)(serviceTicket: ServiceTicket): IO[OppijaAttributes] = {
    val oidPerson = authenticationInfoService.getOnrHenkilo(serviceTicket) // hetu annettu tikettiparametrissa
    oidPerson match {
      case Some(x) =>
        IO.pure(Map("nationalIdentificationNumber" -> oidPerson.get.hetu,
          "personOid" -> oidPerson.get.oidHenkilo,
          "displayName" -> (oidPerson.get.kutsumanimi + " " + oidPerson.get.sukunimi)))
      case None =>
        IO.pure(Map("nationalIdentificationNumber" -> TestFixture.testHetu,
          "personOid" -> TestFixture.personOid,
          "displayName" -> TestFixture.displayName))
    }
  }

  override def fetchCasSession(params: CasParams, sessionCookieName: String): IO[SessionCookie] = {
    IO.pure("keksi")
  }
}
