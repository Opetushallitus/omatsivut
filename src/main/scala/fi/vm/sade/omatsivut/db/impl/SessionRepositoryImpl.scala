package fi.vm.sade.omatsivut.db.impl

import java.util.UUID
import java.util.concurrent.TimeUnit

import fi.vm.sade.omatsivut.db.SessionRepository
import fi.vm.sade.omatsivut.security.{SessionId, OppijaNumero, Session}
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

trait SessionRepositoryImpl extends SessionRepository with OmatsivutRepository {

  override def store(session: Session): SessionId = session match {
    case Session(OppijaNumero(oppijaNumero)) =>
      val id = UUID.randomUUID()
      runBlocking(
        sqlu"""insert into sessions (id, oppija_numero)
               values (${id.toString}::uuid, $oppijaNumero)""",
          timeout = Duration(1, TimeUnit.MINUTES))
      SessionId(id)
  }

  override def delete(id: SessionId): Unit = {
    runBlocking(sqlu"""delete from sessions where id = ${id.value.toString}::uuid""", timeout = Duration(10, TimeUnit.SECONDS))
  }

  override def get(sessionId: SessionId): Option[Session] = {
    val id = sessionId.value
    runBlocking(
      sql"""select oppija_numero from sessions
            where id = ${id.value.toString}::uuid and viimeksi_luettu > now() - interval '60 minutes'
      """.as[Option[String]].map(_.headOption).flatMap {
        case None =>
          sqlu"""delete from sessions where id = ${id.value.toString}::uuid""".andThen(DBIO.successful(None))
        case Some(t) =>
          sqlu"""update sessions set viimeksi_luettu = now()
                 where id = ${id.value.toString}::uuid and viimeksi_luettu < now() - interval '30 minutes'"""
            .andThen(DBIO.successful(Some(t)))
      }.transactionally, Duration(2, TimeUnit.SECONDS)
    ).map {
      case oppijaNumero =>
        Session(OppijaNumero(oppijaNumero.getOrElse("")))
    }
  }

}
