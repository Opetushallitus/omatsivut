package fi.vm.sade.omatsivut

import java.time.Duration

import fi.vm.sade.jetty.OpintopolkuJetty
import org.slf4j.LoggerFactory

class OmatsivutServer extends OpintopolkuJetty

object OmatsivutServer {
  val logger = LoggerFactory.getLogger(classOf[OmatsivutServer])

  def main(args: Array[String]): Unit = {
    logger.info("About to start OmatsivutServer")
    new OmatsivutServer().start(
      "/omatsivut",
      Integer.valueOf(System.getProperty("omatsivut.port", "7667")),
      5,
      10,
      Duration.ofMinutes(1)
    )
  }
}
