package fi.vm.sade.omatsivut.util

import java.io.IOException
import java.net.Socket

object PortChecker {
  def isFreeLocalPort(port: Int): Boolean = {
    try {
      val socket = new Socket("127.0.0.1", port)
      socket.close()
      false
    } catch {
      case e:IOException => true
    }
  }
  def findFreeLocalPort(basePort: Int = 8080) = {
    (basePort to (basePort + 2000)).find(isFreeLocalPort(_)).get
  }
}
