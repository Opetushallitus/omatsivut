package fi.vm.sade.omatsivut.valintatulokset

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.json.JsonFormats

trait ValintatulosService {
  def getValintatulos(hakemusOid: String, hakuOid: String): Option[Valintatulos]
}

object ValintatulosService {
  def apply(implicit appConfig: AppConfig) = {
    MockValintatulosService()
  }
}

case class MockValintatulosService() extends ValintatulosService with JsonFormats {
  import org.json4s.jackson.JsonMethods._
  val json = """{"hakemusOid":"1.2.246.562.11.00000878229","hakutoiveet":[{"hakukohdeOid":"1.2.246.562.5.72607738902","tarjoajaOid":"1.2.246.562.10.591352080610","tila":"HYVAKSYTTY","vastaanottotieto":"ILMOITETTU","ilmoittautumisTila":null,"jonosija":1,"varasijanNumero":null}]}"""

  override def getValintatulos(hakemusOid: String, hakuOid: String) = {
    Some(parse(json).extract[Valintatulos])
  }
}

case class Valintatulos(
                                             hakemusOid: String,
                                             hakutoiveet: List[HakutoiveenValintatulos])

case class HakutoiveenValintatulos(
                                              hakukohdeOid: String,
                                              tarjoajaOid: String,
                                              tila: String,
                                              vastaanottotieto: String,
                                              ilmoittautumisTila: String,
                                              jonosija: Option[Int],
                                              varasijaNumero: Option[Int])