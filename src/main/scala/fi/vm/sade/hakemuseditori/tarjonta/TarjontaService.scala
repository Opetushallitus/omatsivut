package fi.vm.sade.hakemuseditori.tarjonta

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde}

trait TarjontaService {
  def haku(oid: String, lang: Language.Language) : Option[Haku]
  def hakukohde(oid: String, lang: Language.Language) : Option[Hakukohde]

  def getOhjeetUudelleOpiskelijalle(hakukohdeOid: Option[String], lang: Language.Language): Option[String] = {
    None
  }
}
