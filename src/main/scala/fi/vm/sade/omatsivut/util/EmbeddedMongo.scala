package fi.vm.sade.omatsivut.util

import de.svenkubiak.embeddedmongodb.EmbeddedMongoDB

object EmbeddedMongo extends Logging {

  def start(portChooser: PortChooser): Option[EmbeddedMongoDB] = {
    if (PortChecker.isFreeLocalPort(portChooser.chosenPort)) {
      logger.info("Starting embedded mongo on port " + portChooser.chosenPort)
      Option(EmbeddedMongoDB.create()
        .withHost("localhost")
        .withPort(portChooser.chosenPort)
        .start());
    } else {
      logger.info("Not starting embedded mongo, seems to be running on port " + portChooser.chosenPort)
      None
    }
  }

}

