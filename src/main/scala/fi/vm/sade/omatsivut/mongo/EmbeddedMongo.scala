package fi.vm.sade.omatsivut.mongo

import de.flapdoodle.embed.mongo.config.{IMongodConfig, MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodStarter}
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network
import fi.vm.sade.omatsivut.PortChecker

object EmbeddedMongo {
  val port = 28018

  def start = {
    if (PortChecker.isFreeLocalPort(port)) {
      Some(new MongoServer(port))
    } else {
      None
    }
  }
}

class MongoServer(val port: Int) {
  private val mongodConfig: IMongodConfig = new MongodConfigBuilder()
    .version(Version.Main.PRODUCTION)
    .net(new Net(port, Network.localhostIsIPv6))
    .build
  private val runtimeConfig = new RuntimeConfigBuilder()
    .defaults(Command.MongoD)
    .processOutput(ProcessOutput.getDefaultInstanceSilent())
    .build();
  private val runtime: MongodStarter = MongodStarter.getInstance(runtimeConfig)
  private val mongodExecutable = runtime.prepare(mongodConfig)
  private val mongod = mongodExecutable.start

  def stop {
    mongod.stop
    mongodExecutable.stop
  }
}