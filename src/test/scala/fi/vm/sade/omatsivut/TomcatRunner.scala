package fi.vm.sade.omatsivut

import java.io.File

import org.apache.catalina.startup.Tomcat

@deprecated("use JettyLauncher instead")
object TomcatRunner extends App {
  JettyLauncher.main(args)
}