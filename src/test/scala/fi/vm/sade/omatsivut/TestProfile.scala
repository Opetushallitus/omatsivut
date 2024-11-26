package fi.vm.sade.omatsivut

import java.io.{File, FilenameFilter}

import fi.vm.sade.omatsivut.config.{AppConfig, ComponentRegistry}
import fi.vm.sade.omatsivut.util.Logging
import org.apache.commons.io.FileUtils

object SharedAppConfig {
  lazy final val appConfig = new AppConfig.IT
  lazy val componentRegistry: ComponentRegistry = {
    appConfig.onStart()
    val registry = new ComponentRegistry(appConfig)
    registry
  }
}

object SharedJetty {
  private lazy val jettyLauncher = new JettyLauncher(Some("it"))
  Runtime.getRuntime.addShutdownHook(new TempDirCleanUpThread)

  def start {
    jettyLauncher.start
  }

  def main(args: Array[String]) = start
}

class TempDirCleanUpThread extends Thread with Logging {
  override def run(): Unit = {
    logger.info("Running shutdown hook for cleanup.")
    val tmpDir = System.getProperty("java.io.tmpdir")
    logger.info(s"Deleting jetty-* and scalate-* from $tmpDir")
    new File(tmpDir).listFiles(new FilenameFilter {
      override def accept(dir: File, name: String): Boolean = {
        name.startsWith("jetty-") || name.startsWith("scalate-")
      }
    }).foreach(f => {
      logger.info(s"\tremoving $f")
      FileUtils.forceDelete(f)
    })
  }
}
