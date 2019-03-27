package fi.vm.sade.omatsivut

import fi.vm.sade.jetty.OpintopolkuJetty
import org.slf4j.LoggerFactory

class OmatsivutServer extends OpintopolkuJetty {

}

object OmatsivutServer {
  val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    logger.info("About to start OmatsivutServer")
    new OmatsivutServer().start("/omatsivut")
  }
}
