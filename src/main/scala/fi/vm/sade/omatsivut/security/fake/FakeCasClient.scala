package fi.vm.sade.omatsivut.security.fake

import fi.vm.sade.javautils.nio.cas.CasClient
import fi.vm.sade.omatsivut.fixtures.TestFixture
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import org.asynchttpclient

import java.util
import java.util.concurrent.CompletableFuture
import scala.collection.JavaConverters.mapAsJavaMapConverter

class FakeCasClient(authenticationInfoService: AuthenticationInfoService) extends CasClient {

  override def validateServiceTicketWithOppijaAttributes(service: String, ticket: String): CompletableFuture[util.HashMap[String, String]] = {
    val oidPerson = authenticationInfoService.getOnrHenkilo(ticket) // vippaskonsti
    val result: util.HashMap[String, String] = oidPerson match {
      case Some(x) =>
        new util.HashMap(Map(
          "nationalIdentificationNumber" -> oidPerson.get.hetu,
          "personOid" -> oidPerson.get.oidHenkilo,
          "displayName" -> (oidPerson.get.kutsumanimi + " " + oidPerson.get.sukunimi)
        ).asJava)
      case None =>
        new util.HashMap(Map(
          "nationalIdentificationNumber" -> TestFixture.testHetu,
          "personOid" -> TestFixture.personOid,
          "displayName" -> TestFixture.displayName
        ).asJava)
    }

    CompletableFuture.completedFuture(result)
  }

  override def execute(request: asynchttpclient.Request): CompletableFuture[asynchttpclient.Response] = ???

  override def executeAndRetryWithCleanSessionOnStatusCodes(request: asynchttpclient.Request, set: util.Set[Integer]): CompletableFuture[asynchttpclient.Response] = ???

  override def validateServiceTicketWithVirkailijaUsername(s: String, s1: String): CompletableFuture[String] = ???
}
