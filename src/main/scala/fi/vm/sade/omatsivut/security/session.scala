package fi.vm.sade.omatsivut.security

import java.util.UUID


case class SessionId(value: UUID)

case class OppijaNumero(value: String)

case class Hetu(value: String)

case class SessionInfo(ticket: String, hetu: Hetu, oppijaNumero: OppijaNumero, oppijaNimi: String)
