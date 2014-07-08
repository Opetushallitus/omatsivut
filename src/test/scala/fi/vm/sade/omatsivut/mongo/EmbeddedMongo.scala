package fi.vm.sade.omatsivut.mongo

import fi.vm.sade.haku.oppija.configuration.MongoServer
import fi.vm.sade.omatsivut.AppConfig
import fi.vm.sade.omatsivut.AppConfig
import fi.vm.sade.omatsivut.AppConfig.AppConfig

object EmbeddedMongo {
  case class MongoWrapper(port: Int, server: MongoServer)

  private var mongoServer: Option[MongoWrapper] = None

  def start = this.synchronized { mongoServer match {
    case None =>
      val port = 28018
      mongoServer = Some(MongoWrapper(port, new MongoServer(port.toString)))
      port
    case Some(mongoWrapper) =>
      mongoWrapper.port
  }}

  def stop = this.synchronized { mongoServer match {
    case None => // already down
    case Some(wrapper) =>
      wrapper.server.destroy()
      mongoServer = None
  }}

  def withEmbeddedMongo(config: AppConfig)(block: => Unit) {
    config match {
      case AppConfig.IT =>
        // TODO: shutdown only if was new instance
        start
        block
        stop
      case _ => block
    }
  }
}
