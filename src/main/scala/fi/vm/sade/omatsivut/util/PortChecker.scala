package fi.vm.sade.omatsivut.util

import java.io.IOException
import java.net.Socket

import scala.util.Random

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
  def findFreeLocalPort: Int = {
    val port = new Random().nextInt(60000) + 1000
    if (isFreeLocalPort(port)) {
      port
    } else {
      findFreeLocalPort
    }
  }
}
