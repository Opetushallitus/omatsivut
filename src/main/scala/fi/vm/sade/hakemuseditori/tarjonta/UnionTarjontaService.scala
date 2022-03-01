package fi.vm.sade.hakemuseditori.tarjonta
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde}

class UnionTarjontaService(highPriorityService : TarjontaService,
                           lowPriorityService : TarjontaService) extends TarjontaService {
  override def haku(oid: String, lang: Language): Option[Haku] = {
    highPriorityService.haku(oid, lang)
      .orElse(lowPriorityService.haku(oid, lang))
  }

  override def hakukohde(oid: String, lang: Language): Option[Hakukohde] = {
    highPriorityService.hakukohde(oid, lang)
      .orElse(lowPriorityService.hakukohde(oid, lang))
  }
}
