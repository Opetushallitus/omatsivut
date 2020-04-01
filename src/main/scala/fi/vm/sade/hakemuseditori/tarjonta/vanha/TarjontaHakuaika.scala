package fi.vm.sade.hakemuseditori.tarjonta.vanha

import fi.vm.sade.hakemuseditori.tarjonta.domain.Hakuaika

sealed case class TarjontaHakuaika(hakuaikaId: String, alkuPvm: Long, loppuPvm: Long)

object TarjontaHakuaika {
  def toHakuaika(tarjontaHakuaika: TarjontaHakuaika): Hakuaika = {
    Hakuaika(tarjontaHakuaika.hakuaikaId, tarjontaHakuaika.alkuPvm, tarjontaHakuaika.loppuPvm)
  }
}
