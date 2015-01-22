package fi.vm.sade.omatsivut.security

import fi.vm.sade.omatsivut.muistilista.XssUtility
import org.specs2.mutable.Specification

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

    "Test subject input cleaning" in {
      val subject = "muistilista otsikko<html></html> § < <1>  ><a href=\\\"http://www.google.com\\\"</a>"
      XssUtility.purifyFromHtml(subject) must_== "muistilista otsikko § < <1> >"
    }
  }

}
