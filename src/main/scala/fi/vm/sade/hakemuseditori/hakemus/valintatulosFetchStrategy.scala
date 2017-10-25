package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.ataru.AtaruApplication
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku

trait ValintatulosFetchStrategy {
  def legacy(haku: Haku, h: ImmutableLegacyApplicationWrapper): Boolean
  def ataru(haku: Haku, h: AtaruApplication): Boolean
}

object Fetch extends ValintatulosFetchStrategy {
  override def legacy(haku: Haku, h: ImmutableLegacyApplicationWrapper): Boolean = true

  override def ataru(haku: Haku, h: AtaruApplication): Boolean = true
}

object DontFetch extends ValintatulosFetchStrategy {
  override def legacy(haku: Haku, h: ImmutableLegacyApplicationWrapper): Boolean = false

  override def ataru(haku: Haku, h: AtaruApplication): Boolean = false
}

object FetchIfNoHetuOrToinenAste extends ValintatulosFetchStrategy {
  override def legacy(haku: Haku, h: ImmutableLegacyApplicationWrapper): Boolean =
    h.henkilotunnus.isEmpty || haku.toisenasteenhaku

  override def ataru(haku: Haku, h: AtaruApplication): Boolean = false
}
