package fi.vm.sade.hakemuseditori.tarjonta

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde, KohteenHakuaika}

trait TarjontaService {
  def haku(oid: String, lang: Language.Language) : Option[Haku]
  def hakukohde(oid: String) : Option[Hakukohde]

  def filterHakutoiveOidsByActivity(activity: Boolean, hakutoiveet: List[Hakemus.HakutoiveData], haku: Haku): List[String] = {
    val hakukohteet = hakutoiveet.flatMap(entry => entry.get("Koulutus-id").map(oid => {
      hakukohde(oid).getOrElse(Hakukohde(oid, None, None, Some(KohteenHakuaika(0L, 0L)), None, false))
    }))
    hakukohteet.filter(hakukohde => hakukohde.kohteenHakuaika match {
      case Some(aika) => aika.active == activity
      case _ => hakukohde.hakuaikaId.map((hakuaikaId: String) => haku.applicationPeriods.find(_.id == hakuaikaId).exists(_.active == activity)).getOrElse(haku.active == activity)
    }).map(_.oid)
  }

  def getOhjeetUudelleOpiskelijalle(hakukohdeOid: Option[String]): Option[String] = {
    for {
      oid <- hakukohdeOid
      tarjonnanHakukohde <- hakukohde(oid)
      linkki <- tarjonnanHakukohde.ohjeetUudelleOpiskelijalle
    } yield {
      linkki
    }
  }
}
