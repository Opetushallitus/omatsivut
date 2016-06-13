package fi.vm.sade.omatsivut.mocha

import java.io.{ByteArrayOutputStream, PrintWriter}

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
    val stdoutStream = new ByteArrayOutputStream
    val stderrStream = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdoutStream)
    val stderrWriter = new PrintWriter(stderrStream)

    val pb = Seq("node_modules/mocha-phantomjs/bin/mocha-phantomjs", "-R", "xunit", "http://localhost:"+AppConfig.embeddedJettyPortChooser.chosenPort+"/omatsivut/test/runner.html?grep=monta%20hakuaikaa")
    val res = pb.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()

    val plainTestRunOutput: String = stdoutStream.toString
    val testOutput = plainTestRunOutput.split("\n").filter(_.startsWith("<")).mkString("\n")
    new PrintWriter("target/surefire-reports/mocha-tests.xml") {
      write(testOutput)
      close()
    }
    System.err.println(stderrStream.toString)
    println(s"Test output:\n$plainTestRunOutput")

    if (res != 0) {
      failure("Mocha tests failed")
    } else {
      success
    }
  }
}
