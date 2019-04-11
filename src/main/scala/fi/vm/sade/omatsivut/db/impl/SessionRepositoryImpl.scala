package fi.vm.sade.omatsivut.db.impl

import java.util.UUID
import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.omatsivut.security.{SessionId, Hetu, OppijaNumero, SessionInfo}
import slick.jdbc.PostgresProfile.api._

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

  override def get(sessionId: SessionId): Option[SessionInfo] = {
    val id = sessionId.value
    runBlocking(
      sql"""select hetu, oppija_numero, oppija_nimi from sessions
            where id = ${id.value.toString}::uuid and viimeksi_luettu > now() - interval '#${sessionTimeoutSeconds} seconds'
      """.as[(String, Option[String], String)].map(_.headOption).flatMap {
        case None =>
          sqlu"""delete from sessions where id = ${id.value.toString}::uuid""".andThen(DBIO.successful(None))
        case Some(t) =>
          sqlu"""update sessions set viimeksi_luettu = now()
                 where id = ${id.value.toString}::uuid and viimeksi_luettu < now() - interval '#${sessionTimeoutSeconds / 2} seconds'"""
            .andThen(DBIO.successful(Some(t)))
      }.transactionally, Duration(20, TimeUnit.SECONDS)
    ).map {
      case (hetu, oppijaNumero, oppijaNimi) =>
        SessionInfo(Hetu(hetu), OppijaNumero(oppijaNumero.getOrElse("")), oppijaNimi)
    }
  }

}
