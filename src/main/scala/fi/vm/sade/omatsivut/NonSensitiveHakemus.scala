package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakutoive
import fi.vm.sade.omatsivut.NonSensitiveHakemus.Oid

case class NonSensitiveHakemus(oid: Oid, hakutoiveet: List[Hakutoive])

object NonSensitiveHakemus {
  type Oid = String
}
