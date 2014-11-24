package fi.vm.sade.omatsivut.tarjonta.domain

sealed case class TarjontaHakuaika(hakuaikaId: String, alkuPvm: Long, loppuPvm: Long)
