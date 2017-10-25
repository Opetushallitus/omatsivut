package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.ataru.AtaruApplication

trait ValintatulosFetchStrategy {
  def legacy(h: ImmutableLegacyApplicationWrapper): Boolean
  def ataru(h: AtaruApplication): Boolean
}

object Fetch extends ValintatulosFetchStrategy {
  override def legacy(h: ImmutableLegacyApplicationWrapper): Boolean = true

  override def ataru(h: AtaruApplication): Boolean = true
}

object DontFetch extends ValintatulosFetchStrategy {
  override def legacy(h: ImmutableLegacyApplicationWrapper): Boolean = false

  override def ataru(h: AtaruApplication): Boolean = false
}

object FetchIfNoHetu extends ValintatulosFetchStrategy {
  override def legacy(h: ImmutableLegacyApplicationWrapper): Boolean =
    h.henkilotunnus.isEmpty

  override def ataru(h: AtaruApplication): Boolean = false // FIXME
}
