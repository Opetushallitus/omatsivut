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
        response.getContentType() must_== "text/html; charset=UTF-8"
        // henkilötiedot
        body must contain("""<div class="question"><label>Sukunimi</label><span class="answer">Testaaja</span>""")
        body must contain("""<div class="question"><label>Äidinkieli</label><span class="answer">suomi</span>""")
        // koulutustausta
        body must contain("""<div class="question"><label>Valitse tutkinto, jolla haet koulutukseen</label><span class="answer">Perusopetuksen oppimäärä</span>""")
        // hakutoiveet
        body must contain("""<li class="preference-row"><span class="index">1</span><span class="learning-institution"><label>Opetuspiste</label><span>Kallion lukio</span></span><span class="education"><label>Koulutus</label><span>Lukion ilmaisutaitolinja</span></span></li>""")
        // arvosanat
        body must contain("""<tr><td>Äidinkieli ja kirjallisuus</td><td>Suomi äidinkielenä</td><td>9</td><td></td><td></td></tr>""")
        // lupatiedot
        body must contain("""<label>Minulle saa lähettää postia ja sähköpostia vapaista opiskelupaikoista ja muuta koulutusmarkkinointia.</label><span class="answer">Ei</span>""")

        // TODO: kymppi
        // TODO: ei arvosanaa
      }
    }

    "support additional questions per preference" in {
      authGet("/applications/preview/" + TestFixture.hakemus3, personOid) {
        println(prettyPrintHtml(body))
        // hakutoiveet
        body must contain("""<div class="questions"><div class="question"><label>Haetko urheilijan ammatilliseen koulutukseen?</label><span class="answer">Kyllä</span></div><div class="question"><label>Haluaisitko suorittaa lukion ja/tai ylioppilastutkinnon samaan aikaan kuin ammatillisen perustutkinnon?</label><span class="answer">Kyllä</span></div></div>""")
      }
    }
  }

  private def prettyPrintHtml(content: String) = {
    val prettier = new scala.xml.PrettyPrinter(80, 4)
    prettier.format(scala.xml.XML.loadString(content))
  }

  addServlet(new ApplicationsServlet(), "/*")
}
