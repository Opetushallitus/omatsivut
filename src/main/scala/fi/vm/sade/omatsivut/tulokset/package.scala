package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.tulokset.HakutoiveenValintatulosTila.HakutoiveenValintatulosTila
import fi.vm.sade.omatsivut.tulokset.ResultState.ResultState
import fi.vm.sade.omatsivut.tulokset.VastaanotettavuusTila.VastaanotettavuusTila
import fi.vm.sade.omatsivut.valintatulokset.domain.HakutoiveenIlmoittautumistila

package object tulokset {
  case class Koulutus(oid: String, name: String)
  case class Opetuspiste(oid: String, name: String)

  object ResultState extends Enumeration {
    type ResultState = Value
    val VASTAANOTTANUT, EI_VASTAANOTETTU_MAARA_AIKANA, EHDOLLISESTI_VASTAANOTTANUT, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN = Value
  }

  case class HakemuksenValintatulos(hakutoiveet: List[ToiveenValintatulos])
  case class ToiveenValintatulos(
                                  koulutus: Koulutus,
                                  opetuspiste: Opetuspiste,
                                  tila: HakutoiveenValintatulosTila,
                                  vastaanottotila: ResultState,
                                  vastaanotettavuustila: VastaanotettavuusTila,
                                  vastaanotettavissaAsti: Option[Long],
                                  ilmoittautumistila: Option[HakutoiveenIlmoittautumistila],
                                  jonosija: Option[Int],
                                  varasijojaTaytetaanAsti: Option[Long],
                                  varasijanumero: Option[Int],
                                  tilankuvaus: Option[String]) {
    def isPeruuntunut = {
      vastaanottotila == ResultState.PERUNUT || vastaanottotila == ResultState.PERUUTETTU || vastaanottotila == ResultState.PERUUNTUNUT
    }
  }


  object HakutoiveenValintatulosTila extends Enumeration {
    type HakutoiveenValintatulosTila = Value
    val HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARALLA, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN = Value
  }

  object VastaanotettavuusTila extends Enumeration {
    type VastaanotettavuusTila = Value
    val EI_VASTAANOTETTAVISSA, VASTAANOTETTAVISSA_SITOVASTI, VASTAANOTETTAVISSA_EHDOLLISESTI = Value
  }
}
