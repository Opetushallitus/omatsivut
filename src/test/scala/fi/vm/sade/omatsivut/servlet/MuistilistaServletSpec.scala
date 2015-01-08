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
      postJSON("muistilista", Serialization.write(Muistilista(Some("lahettaja"), "otsikko", "FI", List("foobar@example.com"), List("1.2.246.562.14.2013120511174558582514")))) {
        status must_== 200
        println("body="+body)
        body.isEmpty must_== false
      }
    }
  }

}
