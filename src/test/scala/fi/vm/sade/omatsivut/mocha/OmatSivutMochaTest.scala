package fi.vm.sade.omatsivut.mocha

import fi.vm.sade.omatsivut.JettyLauncher
import fi.vm.sade.omatsivut.util.PortChecker
import org.specs2.mutable.Specification

class OmatSivutMochaTest extends Specification {
  import scala.sys.process._

  "Mocha tests" in {
    System.setProperty("omatsivut.profile", "it")
    val omatSivutPort: Int = PortChecker.findFreeLocalPort(8080)
    new JettyLauncher(omatSivutPort).withJettyAndValintatulosService {
      val pb = Seq("node_modules/mocha-phantomjs/bin/mocha-phantomjs", "-R", "spec", "http://localhost:"+omatSivutPort+"/omatsivut/test/runner.html")
      val res = pb.!
      if (res != 0) {
        failure("Mocha tests failed")
      } else {
        success
      }
    }
  }
}
