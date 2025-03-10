package fi.vm.sade.omatsivut.db.impl

import java.util.ConcurrentModificationException
import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.util.Logging
import javax.sql.DataSource
import org.postgresql.util.PSQLException
import org.springframework.util.ReflectionUtils
import slick.dbio._
import slick.jdbc.PostgresProfile.api.jdbcActionExtensionMethods
import slick.jdbc.PostgresProfile.backend.Database
import slick.jdbc.TransactionIsolation.Serializable

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.control.NonFatal


// inspired by (copied from) fi/vm/sade/valintatulosservice/valintarekisteri/db/impl/ValintarekisteriRepository.scala
trait OmatsivutRepository extends Logging {
  private val logSqlOfSomeQueries = false // For debugging only. Do NOT enable in production.

  val db: Database
  val dataSource: DataSource
  def runBlocking[R](operations: DBIO[R], timeout: Duration = Duration(10, TimeUnit.MINUTES)): R = {
    if (logSqlOfSomeQueries) {
      logger.error("This should not happen in production.")
      operations.getClass.getDeclaredFields.foreach { f =>
        ReflectionUtils.makeAccessible(f)
        if (f.getName.startsWith("query")) {
          val value = f.get(operations).toString.replaceAll("\n", " ").replaceAll("\r", " ")
          logger.error(s"QUERY: $value")
        }
      }
    }
    Await.result(
      db.run(operations.withStatementParameters(statementInit = st => st.setQueryTimeout(timeout.toSeconds.toInt))),
      timeout + Duration(1, TimeUnit.SECONDS)
    )
  }
  def runBlockingTransactionally[R](operations: DBIO[R], timeout: Duration = Duration(20, TimeUnit.SECONDS)): Either[Throwable, R] = {
    val SERIALIZATION_VIOLATION = "40001"
    try {
      Right(runBlocking(operations.transactionally.withTransactionIsolation(Serializable), timeout))
    } catch {
      case e: PSQLException if e.getSQLState == SERIALIZATION_VIOLATION =>
        Left(new ConcurrentModificationException(s"Operation(s) failed because of an concurrent action.", e))
      case NonFatal(e) => Left(e)
    }
  }
}
