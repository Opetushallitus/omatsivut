package fi.vm.sade.omatsivut.mongo

import fi.vm.sade.haku.oppija.configuration.MongoServer

object EmbeddedMongo {
  private var mongoServer: Option[MongoServer] = None

  def start = mongoServer match {
    case None => mongoServer = Some(new MongoServer("28018"))
    case _ => throw new IllegalStateException("Mongo server already running")
  }

  def stop = mongoServer match {
    case None => // already down
    case Some(mongoServer) => mongoServer.destroy()
  }
}
