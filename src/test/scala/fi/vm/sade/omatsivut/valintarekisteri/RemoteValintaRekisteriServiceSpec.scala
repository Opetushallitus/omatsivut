package fi.vm.sade.omatsivut.valintarekisteri

import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.utils.http.{HttpClient, HttpRequest}
import org.json4s.jackson.Serialization
import org.junit.runner.RunWith
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock._
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class RemoteValintaRekisteriServiceSpec extends MutableScalatraSpec with Mockito with JsonFormats {

  val henkilo = "foo"
  val hakukohde = "bar"
  val ilmoittaja = "foo"
  val serviceUrl = "http://localhost/valintarekisteri/vastaanotto"

  "RemoteValintaRekisteriService" should {

    "send a POST request" in {
      vastaanota(henkilo, hakukohde, ilmoittaja) should_== true
    }

    "send Caller-Id request header" in {
      vastaanotaAndAssert(henkilo, hakukohde, ilmoittaja)(requestHeaders =>
        requestHeaders("Caller-Id") should_== "omatsivut.omatsivut.backend"
      )
    }

    "send Palvelukutsu.Lahettaja.JarjestelmaTunnus request header" in {
      vastaanotaAndAssert(henkilo, hakukohde, ilmoittaja)(requestHeaders =>
        requestHeaders("Palvelukutsu.Lahettaja.JarjestelmaTunnus") should_== ilmoittaja
      )
    }

  }
  
  def vastaanota(henkilo: String, hakukohde: String, ilmoittaja: String): Boolean =
    new RemoteValintaRekisteriService(serviceUrl, mockHttpClient()).vastaanota(henkilo, hakukohde, ilmoittaja)
  
  def vastaanotaAndAssert(henkilo: String, hakukohde: String, ilmoittaja: String)(requestAssert: (Map[String, String]) => Unit) = {
    new RemoteValintaRekisteriService(serviceUrl, mockHttpClient(requestAssert)).vastaanota(henkilo, hakukohde, ilmoittaja)
  }

  def mockHttpClient(requestAssert: (Map[String, String]) => Unit = (requestHeaders: Map[String, String]) => {}) = {
    val client = mock[HttpClient]
    client.httpPost(
      serviceUrl,
      Some(Serialization.write(VastaanottoIlmoitus(henkilo, hakukohde, ilmoittaja)))
    ) returns MockedHttpRequest(
      serviceUrl,
      200,
      Some(""),
      requestAssert
    )
    client
  }
}

case class MockedHttpRequest(url: String, code: Int, resp: Option[String], requestAssert: (Map[String, String]) => Unit) extends HttpRequest {
  var params: Map[String, String] = Map()
  var headers: Map[String, String] = Map()
  override def responseWithHeaders(): (Int, Map[String, String], String) = {
    requestAssert(headers)
    (code, Map(), resp.getOrElse(""))
  }
  override def header(key: String, value: String): HttpRequest = {
    headers = headers + (key -> value)
    this
  }
  override def param(key: String, value: String): HttpRequest = {
    params = params + (key -> value)
    this
  }
  override def response(): Option[String] = resp
  override def getUrl: String = url
}

