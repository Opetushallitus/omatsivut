package fi.vm.sade.omatsivut.tarjonta

case class TarjontaHaku(oid: String, hakuaikas: List[TarjontaHakuaika],
                        hakutapaUri: String, hakutyyppiUri: String, kohdejoukkoUri: String,
                        usePriority: Boolean, nimi: Map[String, String])
