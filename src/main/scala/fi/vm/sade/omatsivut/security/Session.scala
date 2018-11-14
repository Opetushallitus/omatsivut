package fi.vm.sade.omatsivut.security

import java.util.UUID

case class SessionId(value: UUID)

case class OppijaNumero(value: String)

case class Session(oppijaNumero: OppijaNumero)
