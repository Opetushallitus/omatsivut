package fi.vm.sade.hakemuseditori.tarjonta.vanha

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.http.HttpCall
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaService
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde}
import fi.vm.sade.omatsivut.OphUrlProperties

trait RemoteTarjontaComponent {
  this: OhjausparametritComponent =>

  class RemoteTarjontaService() extends TarjontaService with HttpCall {
    override def haku(oid: String, lang: Language.Language) : Option[Haku] = {
      withHttpGet("Tarjonta fetch haku", OphUrlProperties.url("tarjonta-service.haku", oid), {_.flatMap(TarjontaParser.parseHaku).map({ tarjontaHaku =>
        val haunAikataulu = ohjausparametritService.haunAikataulu(oid)
        Haku(tarjontaHaku, lang).copy(aikataulu = haunAikataulu)
      })}
      )
    }

    override def hakukohde(oid: String): Option[Hakukohde] = {
      if (oid != "") {
        withHttpGet( "Tarjonta fetch hakukohde", OphUrlProperties.url("tarjonta-service.hakukohde", oid), {_.flatMap(TarjontaParser.parseHakukohde)})
      } else {
        None
      }
    }
  }

}
