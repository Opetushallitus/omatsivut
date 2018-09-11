package fi.vm.sade.omatsivut.mocha

import fi.vm.sade.omatsivut.SharedJetty
import fi.vm.sade.omatsivut.config.AppConfig
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OmatSivutMochaTest extends Specification {
  import scala.sys.process._

  step {
    SharedJetty.start
  }

  "Mocha tests" in {
    val pb: Seq[String] = Seq("node_modules/mocha-headless-chrome/bin/start",
      "-f",
      "http://localhost:"+AppConfig.embeddedJettyPortChooser.chosenPort+"/omatsivut/test/runner.html")
    val res: Int = pb.!
    if (res != 0) {
      failure("Mocha tests failed")
    } else {
      success
    }
  }
}
