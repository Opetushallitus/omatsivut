package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo

class NonSensitiveHakemusInfo(sensitiveHakemusInfo: HakemusInfo, _jsonWebToken: String) {
  val hakemusInfo = sensitiveHakemusInfo.copy(
    hakemus = sensitiveHakemusInfo.hakemus.copy(answers = Map.empty)
  )
  val jsonWebToken = _jsonWebToken
}

object NonSensitiveHakemus {
  type Oid = String
}