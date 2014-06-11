package fi.vm.sade.omatsivut

import org.scalatra.test.specs2._

// For more on Specs2, see http://etorreborre.github.com/specs2/guide/org.specs2.guide.QuickStart.html
class OHPServletSpec extends ScalatraSpec { def is =
  "GET / on OHPServlet"                     ^
    "should return status 200"                  ! root200^
                                                end

  implicit val swagger = new OHPSwagger
  
  addServlet(new OHPServlet, "/*")

  def root200 = get("/") {
    status must_== 200
  }
}
