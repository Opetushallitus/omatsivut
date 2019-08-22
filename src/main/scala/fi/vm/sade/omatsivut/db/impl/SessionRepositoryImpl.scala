package fi.vm.sade.omatsivut.db.impl

import java.util.UUID
import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.SessionFailure
import fi.vm.sade.omatsivut.SessionFailure.SessionFailure
import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.omatsivut.security.{Hetu, OppijaNumero, SessionId, SessionInfo}
import slick.jdbc.PostgresProfile.api._
import slick.sql.{SqlAction, SqlStreamingAction}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

trait SessionRepositoryImpl extends SessionRepository with OmatsivutRepository {

  override def store(session: SessionInfo): SessionId = session match {
    case SessionInfo(Hetu(hetu), OppijaNumero(oppijaNumero), oppijaNimi) =>
      val id = UUID.randomUUID()
      runBlocking(
        sqlu"""insert into sessions (id, hetu, oppija_numero, oppija_nimi)
               values (${id.toString}::uuid, $hetu, $oppijaNumero, $oppijaNimi)""",
          timeout = Duration(10, TimeUnit.SECONDS))
      SessionId(id)
  }

  override def delete(id: SessionId): Unit = {
    runBlocking(sqlu"""delete from sessions where id = ${id.value.toString}::uuid""", timeout = Duration(10, TimeUnit.SECONDS))
  }

  override def deleteExpired(): Int = {
    val deletedRowsCount = runBlocking(sqlu"""delete from sessions where viimeksi_luettu < now() - interval '#${sessionTimeoutSeconds} seconds'""",
      timeout = Duration(300, TimeUnit.SECONDS))
    deletedRowsCount
  }

  override def get(sessionId: SessionId): Either[SessionFailure, SessionInfo] = {
    val id = sessionId.value
    val sessionQuery: SqlStreamingAction[Vector[(String, Option[String], String, Boolean)], (String, Option[String], String, Boolean), Effect] =
      sql"""select hetu, oppija_numero, oppija_nimi,
              viimeksi_luettu > now() - interval '#${sessionTimeoutSeconds} seconds' as is_valid
            from sessions
            where id = ${id.value.toString}::uuid
      """.as[(String, Option[String], String, Boolean)]

    runBlocking(
      sessionQuery.map(_.headOption).flatMap {
        case None =>
          DBIO.successful(None)
        case Some(t) => {
          val sessionIsValid = t._4
          if (sessionIsValid) {
            sqlu"""update sessions set viimeksi_luettu = now()
                 where id = ${id.value.toString}::uuid and viimeksi_luettu < now() - interval '#${sessionTimeoutSeconds / 2} seconds'"""
              .andThen(DBIO.successful(Some(t)))
          } else {
            sqlu"""delete from sessions where id = ${id.value.toString}::uuid"""
              .andThen(DBIO.successful(Some(t)))
          }
        }
      }.transactionally, Duration(20, TimeUnit.SECONDS)
    ) match {
      case Some((hetu, oppijaNumero, oppijaNimi, true)) =>
        Right(SessionInfo(Hetu(hetu), OppijaNumero(oppijaNumero.getOrElse("")), oppijaNimi))
      case Some((_, _, _, false)) =>
        Left(SessionFailure.SESSION_EXPIRED)
      case None =>
        Left(SessionFailure.SESSION_NOT_FOUND)
    }
  }
}
