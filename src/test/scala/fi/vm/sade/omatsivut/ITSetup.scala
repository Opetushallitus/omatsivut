package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.db.impl.OmatsivutDb
import slick.jdbc.{GetResult, PositionedParameters, SetParameter}

trait ITSetup {
  implicit val appConfig = new AppConfig.IT
  val dbConfig = appConfig.settings.omatsivutDbConfig
  val testSessionTimeout: Int = 100

  lazy val singleConnectionOmatsivutDb = new OmatsivutDb(
    dbConfig.copy(maxConnections = Some(1), minConnections = Some(1)),
    sessionTimeoutSeconds = testSessionTimeout)

  lazy val omatsivutDbWithPool = new OmatsivutDb(dbConfig, true, sessionTimeoutSeconds = testSessionTimeout)
}
