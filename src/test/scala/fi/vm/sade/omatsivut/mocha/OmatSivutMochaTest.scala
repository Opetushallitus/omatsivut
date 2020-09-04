package fi.vm.sade.omatsivut.mocha

import java.util.concurrent.TimeUnit.MINUTES

import fi.vm.sade.omatsivut.SharedJetty
import fi.vm.sade.omatsivut.config.AppConfig
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class OmatSivutMochaTest extends Specification {
  private val totalMochaTestsMaxDuration: Duration = Duration(10, MINUTES)
  import scala.sys.process._

  step {
    SharedJetty.start
  }

  "Mocha tests" in {
    val pb: Seq[String] = Seq("node_modules/mocha-headless-chrome/bin/start",
      "-v",
      "visible-true",
      "-a",
      "window-size=2560,1440",
      "-a",
      "no-sandbox",
      "-a",
      "proxy-server='direct://'",
      "-a",
      "proxy-bypass-list=*",
      "-t",
      totalMochaTestsMaxDuration.toMillis.toString,
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
