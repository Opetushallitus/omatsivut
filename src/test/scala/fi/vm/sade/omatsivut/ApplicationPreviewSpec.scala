package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.domain.Hakemus
import fi.vm.sade.omatsivut.fixtures.{FixtureImporter, TestFixture}
import fi.vm.sade.omatsivut.fixtures.TestFixture._
import fi.vm.sade.omatsivut.servlet.ApplicationsServlet
import org.json4s.jackson.Serialization

class ApplicationPreviewSpec extends HakemusApiSpecification {
  override implicit lazy val appConfig = new AppConfig.IT

  sequential

  "GET /applications/preview/:oid" should {
    "generate application preview" in {
      FixtureImporter().applyOverrides("peruskoulu")
      authGet("/applications/preview/" + TestFixture.hakemus2, personOid) {
        val prettier = new scala.xml.PrettyPrinter(80, 4)
        val content = body
        println(prettier.format(scala.xml.XML.loadString(content)))
        response.getContentType() must_== "text/html; charset=UTF-8"
        // henkilötiedot
        content must contain("""<div class="question"><label>Sukunimi</label><span class="answer">Testaaja</span>""")
        content must contain("""<div class="question"><label>Äidinkieli</label><span class="answer">suomi</span>""")
        // koulutustausta
        content must contain("""<div class="question"><label>Valitse tutkinto, jolla haet koulutukseen</label><span class="answer">Perusopetuksen oppimäärä</span>""")
        // hakutoiveet
        content must contain("""<li class="preference-row"><span class="index">1</span><span class="learning-institution"><label>Opetuspiste</label><span>Kallion lukio</span></span><span class="education"><label>Koulutus</label><span>Lukion ilmaisutaitolinja</span></span><div class="questions"></div></li>""")
        // arvosanat
        content must contain("""<tr><td>Äidinkieli ja kirjallisuus</td><td>Suomi äidinkielenä</td><td>9</td><td></td><td></td></tr>""")
        // lupatiedot
        content must contain("""<label>Minulle saa lähettää postia ja sähköpostia vapaista opiskelupaikoista ja muuta koulutusmarkkinointia.</label><span class="answer">Ei</span>""")

        // TODO: hakutoiveen omat kysymykset
        // TODO: kymppi
        // TODO: ei arvosanaa
      }
    }
  }

  addServlet(new ApplicationsServlet(), "/*")
}
