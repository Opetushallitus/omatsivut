package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.ataru.AtaruApplication
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.Henkilo

trait ValintatulosFetchStrategy {
  def legacy(h: ImmutableLegacyApplicationWrapper): Boolean
  def ataru(a: AtaruApplication, h: Henkilo): Boolean
}

object Fetch extends ValintatulosFetchStrategy {
  override def legacy(h: ImmutableLegacyApplicationWrapper): Boolean = true

  override def ataru(a: AtaruApplication, h: Henkilo): Boolean = true
}

object DontFetch extends ValintatulosFetchStrategy {
  override def legacy(h: ImmutableLegacyApplicationWrapper): Boolean = false

  override def ataru(a: AtaruApplication, h: Henkilo): Boolean = false
}

object FetchIfNoHetu extends ValintatulosFetchStrategy {
  override def legacy(h: ImmutableLegacyApplicationWrapper): Boolean =
    h.henkilotunnus.isEmpty

  override def ataru(a: AtaruApplication, h: Henkilo): Boolean =
    h.hetu.isEmpty
}
