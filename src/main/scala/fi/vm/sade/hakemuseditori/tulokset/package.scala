package fi.vm.sade.hakemuseditori.tulokset

case class Koulutus(oid: String, name: String)

case class Opetuspiste(oid: String, name: String)

object ResultState extends Enumeration {
  type ResultState = Value
  val VASTAANOTTANUT, EI_VASTAANOTETTU_MAARA_AIKANA, EHDOLLISESTI_VASTAANOTTANUT, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN = Value
}

object HakutoiveenValintatulosTila extends Enumeration {
  type HakutoiveenValintatulosTila = Value
  val HYVAKSYTTY, HARKINNANVARAISESTI_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARALLA, PERUUTETTU, PERUNUT, HYLATTY, PERUUNTUNUT, KESKEN = Value
}

object VastaanotettavuusTila extends Enumeration {
  type VastaanotettavuusTila = Value
  val EI_VASTAANOTETTAVISSA, VASTAANOTETTAVISSA_SITOVASTI, VASTAANOTETTAVISSA_EHDOLLISESTI = Value
}
