package fi.vm.sade.omatsivut.util

class PortFromSystemPropertyOrFindFree(systemPropertyName: String) extends PortChooser {
  lazy val chosenPort = System.getProperty(systemPropertyName, PortChecker.findFreeLocalPort.toString).toInt
}
