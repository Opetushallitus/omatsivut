package fi.vm.sade.omatsivut.util

import de.flapdoodle.embed.mongo.config.{ImmutableMongodConfig, MongoCmdOptions, MongodConfig, Net}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.process.config.ImmutableRuntimeConfig
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network

object EmbeddedMongo extends Logging {

  def start(portChooser: PortChooser): Option[MongoServer] = {
    if (PortChecker.isFreeLocalPort(portChooser.chosenPort)) {
      logger.info("Starting embedded mongo on port " + portChooser.chosenPort)
      Some(new MongoServer(portChooser.chosenPort))
    } else {
      logger.info("Not starting embedded mongo, seems to be running on port " + portChooser.chosenPort)
      None
    }
  }

  def withEmbeddedMongo[T](portChooser: PortChooser)(f: => T): T = {
    val mongoServer = start(portChooser)
    try {
      f
    } finally {
      mongoServer.foreach(_.stop())
    }
  }
}

class MongoServer(val port: Int) {
  private val mongodConfig: MongodConfig = ImmutableMongodConfig.builder()
    .version(Version.Main.PRODUCTION)
    .cmdOptions(MongoCmdOptions.builder()
      .storageEngine("ephemeralForTest")
      .build())
    .net(new Net(port, Network.localhostIsIPv6))
    .putParams("maxBSONDepth", "1000")
    .build
  private val runtimeConfig = ImmutableRuntimeConfig.builder()
    .isDaemonProcess(true)
    .processOutput(ProcessOutput.getDefaultInstanceSilent)
    .build();
  private val runtime: MongodStarter = MongodStarter.getInstance(runtimeConfig)
  private val mongodExecutable = runtime.prepare(mongodConfig)
  private val mongod = mongodExecutable.start

  def stop() {
    mongod.stop()
    mongodExecutable.stop()
  }
}
