package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.{ComponentRegistry, AppConfig}
import fi.vm.sade.utils.mongo.EmbeddedMongo

object SharedAppConfig {
  lazy final val appConfig = new AppConfig.IT
  lazy val componentRegistry = {
    val registry = new ComponentRegistry(appConfig)
    registry.start
    registry
  }
}

object SharedJetty {
  private lazy val jettyLauncher = new JettyLauncher(Some("it"))

  def start {
    EmbeddedMongo.start(AppConfig.embeddedMongoPortChooser)
    jettyLauncher.start
  }
}
