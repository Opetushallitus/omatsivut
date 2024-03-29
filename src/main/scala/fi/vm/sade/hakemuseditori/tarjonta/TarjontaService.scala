package fi.vm.sade.hakemuseditori.tarjonta

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde, KohteenHakuaika}
import org.joda.time.LocalDateTime

trait TarjontaService {
  def haku(oid: String, lang: Language.Language) : Option[Haku]
  def hakukohde(oid: String, lang: Language.Language) : Option[Hakukohde]

  def filterHakutoiveOidsByActivity(activity: Boolean, hakutoiveet: List[Hakemus.HakutoiveData], haku: Haku, lang: Language.Language): List[String] = {
    val now = LocalDateTime.now().toDate.getTime
    hakutoiveet
      .flatMap(_.get("Koulutus-id"))
      .flatMap(oid => hakukohde(oid, lang))
      .filter(KohteenHakuaika.active(haku, _, now) == activity)
      .map(_.oid)
  }

  def getOhjeetUudelleOpiskelijalle(hakukohdeOid: Option[String], lang: Language.Language): Option[String] = {
    for {
      oid <- hakukohdeOid
      tarjonnanHakukohde <- hakukohde(oid, lang)
      linkki <- tarjonnanHakukohde.ohjeetUudelleOpiskelijalle
    } yield {
      linkki
    }
  }
}
