package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakutoive
import fi.vm.sade.omatsivut.NonSensitiveHakemus.Oid
import fi.vm.sade.omatsivut.security.JsonWebToken

case class NonSensitiveHakemus(oid: Oid, hakutoiveet: List[Hakutoive], jsonWebToken: String)

object NonSensitiveHakemus {
  type Oid = String
}
