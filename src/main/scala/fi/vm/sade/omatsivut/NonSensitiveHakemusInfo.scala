package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo

object NonSensitiveHakemusInfo {
  type Oid = String

  protected case class NonSensitiveHakemusInfo(hakemusInfo: HakemusInfo, jsonWebToken: String)

  def apply(sensitiveHakemusInfo: HakemusInfo, jsonWebToken: String): NonSensitiveHakemusInfo = {
    NonSensitiveHakemusInfo(
      sensitiveHakemusInfo.copy(
        hakemus = sensitiveHakemusInfo.hakemus.copy(answers = Map.empty)
      ), jsonWebToken)
  }
}