package fi.vm.sade.omatsivut.security

import java.util.UUID

import fi.vm.sade.utils.cas.CasClient.ServiceTicket

case class SessionId(value: UUID)

case class OppijaNumero(value: String)

case class Hetu(value: String)

case class SessionInfo(ticket: ServiceTicket, hetu: Hetu, oppijaNumero: OppijaNumero, oppijaNimi: String)
