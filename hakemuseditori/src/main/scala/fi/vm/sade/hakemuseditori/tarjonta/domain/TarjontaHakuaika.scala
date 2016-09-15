package fi.vm.sade.hakemuseditori.tarjonta.domain

sealed case class TarjontaHakuaika(hakuaikaId: String, alkuPvm: Long, loppuPvm: Long)
