package fi.vm.sade.omatsivut.valintatulokset

case class HakemuksenValintaTulos(
                                   hakemusOid: String,
                                   hakutoiveet: List[HakutoiveenValintaTulos])

case class HakutoiveenValintaTulos(
                                   hakukohdeOid: String,
                                   tarjoajaOid: String,
                                   tila: String,
                                   vastaanottotieto: String,
                                   ilmoittautumisTila: String,
                                   jonosija: Option[Int],
                                   varasijaNumero: Option[Int])

