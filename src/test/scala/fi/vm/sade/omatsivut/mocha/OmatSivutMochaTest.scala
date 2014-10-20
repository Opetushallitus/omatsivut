package fi.vm.sade.omatsivut.mocha

import fi.vm.sade.omatsivut.{JettyLauncher, TomcatRunner}
import fi.vm.sade.omatsivut.config.{ComponentRegistry, AppConfig}
import org.specs2.mutable.Specification

class OmatSivutMochaTest extends Specification {
  import scala.sys.process._

  "Mocha tests" in {
    System.setProperty("omatsivut.profile", "it")
    new JettyLauncher(8080).withJettyAndValintatulosService {
      val pb = Seq("node_modules/mocha-phantomjs/bin/mocha-phantomjs", "-R", "spec", "http://localhost:8080/omatsivut/test/runner.html")
      val res = pb.!
      if (res != 0) {
        failure("Mocha tests failed")
      } else {
        success
      }
    }
  }
}
