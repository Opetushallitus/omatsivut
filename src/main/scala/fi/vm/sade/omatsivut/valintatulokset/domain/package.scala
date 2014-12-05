package fi.vm.sade.omatsivut.valintatulokset

import java.util.Date

package object domain {
  case class Vastaanottoaikataulu(vastaanottoEnd: Option[Date], vastaanottoBufferDays: Option[Int])

  case class Valintatulos(hakemusOid: String, aikataulu: Option[Vastaanottoaikataulu], hakutoiveet: List[HakutoiveenValintatulos])

  case class HakutoiveenValintatulos(hakukohdeOid: String,
                                     tarjoajaOid: String,
                                     valintatila: String,
                                     vastaanottotila: Option[String],
                                     ilmoittautumistila: Option[HakutoiveenIlmoittautumistila],
                                     vastaanotettavuustila: String,
                                     vastaanottoDeadline: Option[Date],
                                     jonosija: Option[Int],
                                     varasijojaKaytetaanAlkaen: Option[Date],
                                     varasijojaTaytetaanAsti: Option[Date],
                                     varasijanumero: Option[Int],
                                     tilanKuvaukset: Map[String, String])

  case class HakutoiveenIlmoittautumistila(
                                            ilmoittautumisaika: Ilmoittautumisaika,
                                            ilmoittautumistapa: Option[Ilmoittautumistapa],
                                            ilmoittautumistila: String,
                                            ilmoittauduttavissa: Boolean
                                            )
  case class Ilmoittautumistapa(
                                 nimi: Option[Map[String, String]],
                                 url: Option[String]
                                 )

  case class Ilmoittautumisaika(alku: Option[Date], loppu: Option[Date])

  case class Vastaanotto(hakukohdeOid: String, tila: String, muokkaaja: String, selite: String)

}
