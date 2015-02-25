package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.muistilista.XssUtility
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class XssPreventionSpec extends Specification {

  "XSS prevention" should {
    "Test email list inputs" in {
      val addressList = List("matti.mallikas@example.com", "maija.meikalainen@example.com", "keijo.kenka@hotmail.com", "tarja.talikka@gmail.com")
      val purifiedList = addressList.map(m => XssUtility.purifyFromHtml(m))
      addressList must_== purifiedList
    }

    "Test failing email input" in {
      val address = "<html>matti.mallikas@example.com</html>"
      val purifiedList = XssUtility.purifyFromHtml(address)
      address must_!= purifiedList
      address must_!= "matti.mallikas@example.com"
    }

    "Test javascript injection" in {
      val maliciousCode = "<IMG SRC=JaVaScRiPt:alert('XSS')>"
      val moreMaliciousCode = "\"><script>...</script><input value=\""
      val remoteStyleSheet = "<LINK REL=\"stylesheet\"HREF=\"http://ha.ckers.org/xss.css\">"
      val brTag = "<BR SIZE=\"&{alert('XSS')}\">"
      val ssi = "<!--#exec cmd=\"/bin/echo '<SCRIPT SRC'\"--><!--#exec cmd=\"/bin/echo '=http://ha.ckers.org/xss.js ></SCRIPT>'\"-->"
      val UTF8encoded = "<IMG SRC=&#106;&#97;&#118;&#97;&# 115;&#99;&#114;&#105;&#112;& #116;&#58;&#97;&#108;&#101;& #114;&#116;&#40;&#39;&#88;&# 83;&#83;&#39;&#41;>"
      val multiLine = "normal\t\n\n<IMG\nSRC\n=\n\"\nj\na\nv\na\ns\nc\nr\ni »\n\np\nt\n:\na\nl\ne\nr\nt\n(\n'\nX\nS\nS\n' »\n\n)\n\"\n> text"
      XssUtility.purifyFromHtml(maliciousCode) must_== ""
      XssUtility.purifyFromHtml(moreMaliciousCode) must_== "\">"
      XssUtility.purifyFromHtml(remoteStyleSheet) must_== ""
      XssUtility.purifyFromHtml(brTag) must_== ""
      XssUtility.purifyFromHtml(ssi) must_== ""
      XssUtility.purifyFromHtml(UTF8encoded) must_== ""
      XssUtility.purifyFromHtml(multiLine) must_== "normal text"
    }

    "Test subject input cleaning" in {
      val subject = "muistilista otsikko<html></html> § < <1>  ><a href=\\\"http://www.google.com\\\"</a>"
      XssUtility.purifyFromHtml(subject) must_== "muistilista otsikko § < <1> >"
    }
  }

}
