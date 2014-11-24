package fi.vm.sade.omatsivut.tarjonta.domain

case class TarjontaHaku(oid: String, hakuaikas: List[TarjontaHakuaika],
                        hakutapaUri: String, hakutyyppiUri: String, kohdejoukkoUri: String,
                        usePriority: Boolean, nimi: Map[String, String])
