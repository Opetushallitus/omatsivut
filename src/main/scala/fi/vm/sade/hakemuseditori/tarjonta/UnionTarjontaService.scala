package fi.vm.sade.hakemuseditori.tarjonta
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde}

class UnionTarjontaService(tarjontaService: TarjontaService,
                           koutaService : TarjontaService) extends TarjontaService {

  def isKoutaOid(oid: String): Boolean = oid.length == 35

  override def haku(oid: String, lang: Language): Option[Haku] = {
    if(isKoutaOid(oid)) koutaService.haku(oid, lang)
    else tarjontaService.haku(oid, lang)
  }

  override def hakukohde(oid: String, lang: Language): Option[Hakukohde] = {
    if(isKoutaOid(oid)) koutaService.hakukohde(oid, lang)
    else tarjontaService.hakukohde(oid, lang)
  }
}
