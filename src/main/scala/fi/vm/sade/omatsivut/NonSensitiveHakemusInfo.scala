package fi.vm.sade.omatsivut

import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._

object NonSensitiveHakemusInfo {
  type Oid = String

  protected case class NonSensitiveHakemusInfo(hakemusInfo: HakemusInfo, jsonWebToken: String)

  def apply(sensitiveHakemusInfo: HakemusInfo, jsonWebToken: String): NonSensitiveHakemusInfo = {
    sensitiveHakemusInfo.hakemus.answers.get(PHASE_PERSONAL) match {
      case Some(henkilotiedot) =>
        val nonSensitiveContactDetails = List(ELEMENT_ID_EMAIL, ELEMENT_ID_PREFIX_PHONENUMBER + "1")
          .map(key => key -> henkilotiedot.getOrElse(key, ""))
          .toMap
        NonSensitiveHakemusInfo(
          sensitiveHakemusInfo.copy(
            hakemus = sensitiveHakemusInfo.hakemus.copy(
              answers = Map(PHASE_PERSONAL -> nonSensitiveContactDetails)
            ),
            questions = List()
          ), jsonWebToken)

      case _ => throw new RuntimeException("henkilotiedot missing for hakemus " + sensitiveHakemusInfo.hakemus.oid)
    }
  }
}