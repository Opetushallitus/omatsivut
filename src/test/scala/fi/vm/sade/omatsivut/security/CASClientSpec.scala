package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.config.AppConfig
import org.specs2.mutable.Specification
import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.http.FakeHttpRequest
import fi.vm.sade.omatsivut.http.FakeHttpClient

class CasClientSpec extends Specification {
  val appConfig = new AppConfig.IT
  val casTicketUrl = appConfig.settings.casTicketUrl

  "CAS client" should {
    "parse ticket granting ticket from Location header" in {
      val client = new CASClient(new FakeHttpClient(new FakeHttpRequest() {
        override def responseWithHeaders = (201, Map[String, List[String]](("Location", List("https://my.cas.serv/some/ticket/url/my-test-ticket"))), "")
      }), casTicketUrl)
      val tgt = client.getTicketGrantingTicket("username", "pasword")
      tgt must_== Some("my-test-ticket")
    }
  }

  "CAS client" should {
    "return no ticket granting ticket when no Location header" in {
      val client = new CASClient(new FakeHttpClient(new FakeHttpRequest() {
        override def responseWithHeaders = (201, Map[String, List[String]](), "")
      }), casTicketUrl)
      val tgt = client.getTicketGrantingTicket("username", "pasword")
      tgt must_== None
    }
  }

  "CAS client" should {
    "return no ticket granting ticket when invalid Location header" in {
      val client = new CASClient(new FakeHttpClient(new FakeHttpRequest() {
        override def responseWithHeaders = (201, Map[String, List[String]](("Location", List("value"))), "")
      }), casTicketUrl)
      val tgt = client.getTicketGrantingTicket("username", "pasword")
      tgt must_== None
    }
  }

  "CAS client" should {
    "return no ticket granting ticket when other than 201 response code" in {
      val client = new CASClient(new FakeHttpClient(new FakeHttpRequest()), casTicketUrl)
      val tgt = client.getTicketGrantingTicket("username", "pasword")
      tgt must_== None
    }
  }
}
