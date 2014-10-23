package fi.vm.sade.omatsivut

@deprecated("use JettyLauncher instead")
object TomcatRunner extends App {
  JettyLauncher.main(args)
}