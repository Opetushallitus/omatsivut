package fi.vm.sade.omatsivut.db.impl

import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import fi.vm.sade.utils.Timer
import org.apache.commons.lang3.builder.ToStringBuilder
import org.flywaydb.core.Flyway
import slick.jdbc.PostgresProfile.api._

case class DbConfig(url: String,
                    user: Option[String],
                    password: Option[String],
                    maxConnections: Option[Int],
                    minConnections: Option[Int],
                    numThreads: Option[Int],
                    queueSize: Option[Int],
                    registerMbeans: Option[Boolean],
                    initializationFailTimeout: Option[Long],
                    leakDetectionThresholdMillis: Option[Long])

class OmatsivutDb(config: DbConfig, itProfile: Boolean = false, override val sessionTimeoutSeconds: Int = 3600) extends OmatsivutRepository
  with SessionRepositoryImpl {

  logger.info(s"Database configuration: ${config.copy(password = Some("***"))}")
  val flyway = new Flyway()
  flyway.setDataSource(config.url, config.user.orNull, config.password.orNull)
  Timer.timed("Flyway migration") { flyway.migrate() }
  override val db = {
    val c = new HikariConfig()
    c.setJdbcUrl(config.url)
    config.user.foreach(c.setUsername)
    config.password.foreach(c.setPassword)
    config.maxConnections.foreach(c.setMaximumPoolSize)
    config.minConnections.foreach(c.setMinimumIdle)
    config.registerMbeans.foreach(c.setRegisterMbeans)
    config.initializationFailTimeout.foreach(c.setInitializationFailTimeout)
    c.setLeakDetectionThreshold(config.leakDetectionThresholdMillis.getOrElse(c.getMaxLifetime))
    val maxConnections = config.numThreads.getOrElse(10)
    val executor = AsyncExecutor("omatsivut",
                                  config.numThreads.getOrElse(10),
                                  config.numThreads.getOrElse(10),
                                  config.queueSize.getOrElse(1000),
                                  config.maxConnections.getOrElse(10))
    logger.info(s"Configured Hikari with ${classOf[HikariConfig].getSimpleName} ${ToStringBuilder.reflectionToString(c).replaceAll("password=.*?,", "password=<HIDDEN>,")}" +
         s" and executor ${ToStringBuilder.reflectionToString(executor)}")
    Database.forDataSource(new HikariDataSource(c), maxConnections = Some(maxConnections), executor)
  }
  if (itProfile) {
    logger.warn("alter table public.schema_version owner to oph")
    runBlocking(sqlu"""alter table public.schema_version owner to oph""")
  }
}
