package fi.vm.sade.omatsivut.valintatulokset

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig.AppConfig

trait ValintatulosService {
  def getValintatulos(application: Application, applicationSystem: ApplicationSystem): Option[HakemuksenValintaTulos]
}

object ValintatulosService {
  def apply(implicit appConfig: AppConfig) = {
    MockValintatulosService()
  }
}

case class MockValintatulosService() extends ValintatulosService {
  override def getValintatulos(application: Application, applicationSystem: ApplicationSystem) = {
    None
  }
}