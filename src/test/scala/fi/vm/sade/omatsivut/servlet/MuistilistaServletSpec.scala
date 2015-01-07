package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.ScalatraTestSupport
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.muistilista.Muistilista
import org.json4s.jackson.Serialization


class MuistilistaServletSpec extends ScalatraTestSupport with JsonFormats {

  override lazy val appConfig = new AppConfig.IT

  "POST muistilista" should {
    "palauttaa KI:sta oideja vastaavat koulutukset" in {
      postJSON("muistilista", Serialization.write(Muistilista(Some("lahettaja"), "otsikko", "FI", List("foobar@example.com"), List("1.2.246.562.20.94964838901")))) {
        status must_== 200
        body.isEmpty must_== false
      }
    }
  }

  def postJSON[T](path: String, body: String, headers: Map[String, String] = Map.empty)(block: => T): T = {
    post(path, body.getBytes("UTF-8"), Map(("Content-type" -> "application/json")) ++ headers)(block)
  }
}
