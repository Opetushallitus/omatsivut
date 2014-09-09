package fi.vm.sade.omatsivut.fixtures

import fi.vm.sade.omatsivut.config.AppConfig
import AppConfig.AppConfig
import fi.vm.sade.omatsivut.valintatulokset.MockValintatulosService

case class ValintatulosFixtureImporter(implicit val appConfig: AppConfig) {
  val json = """{"hakemusOid":"1.2.246.562.11.00000441369","hakutoiveet":[{"hakukohdeOid":"1.2.246.562.5.72607738902","tarjoajaOid":"1.2.246.562.10.591352080610","tila":"HYVAKSYTTY","vastaanottotieto":"ILMOITETTU","ilmoittautumisTila":null,"jonosija":1,"varasijanNumero":null}]}"""

  def applyFixtures(fixtureName: String = "") {
    fixtureName match {
      case "ei-tuloksia" => appConfig.componentRegistry.valintatulosService.asInstanceOf[MockValintatulosService].setMockJson("")
      case "hyvaksytty" => appConfig.componentRegistry.valintatulosService.asInstanceOf[MockValintatulosService].setMockJson(json)
      case _ =>
    }
  }
}