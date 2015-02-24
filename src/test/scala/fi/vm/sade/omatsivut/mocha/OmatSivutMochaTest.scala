package fi.vm.sade.omatsivut.mocha

import fi.vm.sade.omatsivut.JettyLauncher
import fi.vm.sade.utils.tcp.PortChecker
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OmatSivutMochaTest extends Specification {
  import scala.sys.process._

  "Mocha tests" in {
    System.setProperty("omatsivut.profile", "it")
    val omatSivutPort: Int = PortChecker.findFreeLocalPort
    System.setProperty("omatsivut.port", omatSivutPort.toString)

    new JettyLauncher(omatSivutPort).withJetty {
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
