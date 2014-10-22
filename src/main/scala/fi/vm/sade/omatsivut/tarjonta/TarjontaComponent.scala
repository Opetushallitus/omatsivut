package fi.vm.sade.omatsivut.tarjonta

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.http.HttpCall
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.memoize.TTLOptionalMemoize
import fi.vm.sade.omatsivut.ohjausparametrit.OhjausparametritComponent
import org.json4s.JsonAST.JValue

trait TarjontaComponent {
  this: OhjausparametritComponent =>

  class StubbedTarjontaService extends TarjontaService with JsonFormats {
    override def haku(oid: String, lang: Language.Language) = {
      val haku = JsonFixtureMaps.findByKey[JValue]("/mockdata/haut.json", oid).flatMap(TarjontaParser.parseHaku).map {h => Haku(h, lang)}
      haku.map {h =>
        val haunAikataulu = ohjausparametritService.haunAikataulu(oid)
        h.copy(aikataulu = haunAikataulu)
      }
    }

    override def hakukohde(oid: String): Option[Hakukohde] = {
      JsonFixtureMaps.findByKey[JValue]("/mockdata/hakukohteet.json", oid).flatMap(TarjontaParser.parseHakukohde)
    }
  }

  object CachedRemoteTarjontaService {
    def apply(implicit appConfig: AppConfig): TarjontaService = {
      val service = new RemoteTarjontaService()
      val hakuMemo = TTLOptionalMemoize.memoize(service.haku _, 60 * 60)
      val hakukohdeMemo = TTLOptionalMemoize.memoize(service.hakukohde _, 60 * 60)


      new TarjontaService {
        override def haku(oid: String, lang: Language): Option[Haku] = hakuMemo(oid, lang)
        override def hakukohde(oid: String): Option[Hakukohde] = hakukohdeMemo(oid)
      }
    }
  }

  class RemoteTarjontaService(implicit appConfig: AppConfig) extends TarjontaService with HttpCall {

    override def haku(oid: String, lang: Language.Language) : Option[Haku] = {
      withHttpGet("Tarjonta fetch haku", appConfig.settings.tarjontaUrl + "/haku/" + oid, {_.flatMap(TarjontaParser.parseHaku).map({ tarjontaHaku =>
          val haunAikataulu = ohjausparametritService.haunAikataulu(oid)
          Haku(tarjontaHaku, lang).copy(aikataulu = haunAikataulu)
        })}
      )
    }

    override def hakukohde(oid: String): Option[Hakukohde] = {
      withHttpGet( "Tarjonta fetch hakukohde", appConfig.settings.tarjontaUrl + "/hakukohde/" + oid, {_.flatMap(TarjontaParser.parseHakukohde)})
    }
  }
}

case class TarjontaHaku(oid: String, hakuaikas: List[TarjontaHakuaika], hakutapaUri: String, hakutyyppiUri: String, kohdejoukkoUri: String, usePriority: Boolean, nimi: Map[String, String])
case class TarjontaHakuaika(hakuaikaId: String, alkuPvm: Long, loppuPvm: Long)

private object TarjontaParser extends JsonFormats {

  def parseHaku(json: JValue) = {
    for {
      obj <- (json \ "result").toOption
      h <- obj.extractOpt[TarjontaHaku]
    } yield h
  }

  def parseHakukohde(json: JValue) = {
    for {
      obj <- (json \ "result").toOption
      oid = (obj \ "oid").extract[String]
      hakuaikaId = (obj \ "hakuaikaId").extractOpt[String]
      hakuaika = createHakuaika((obj \ "hakuaikaAlkuPvm").extractOpt[Long], (obj \ "hakuaikaLoppuPvm").extractOpt[Long])
    } yield Hakukohde(oid, hakuaikaId, hakuaika)
  }

  private def createHakuaika(hakuaikaAlkuPvm: Option[Long], hakuaikaLoppuPvm: Option[Long]) : Option[KohteenHakuaika] = {
    (hakuaikaAlkuPvm, hakuaikaLoppuPvm) match {
      case (Some(a), Some(l)) => Some(KohteenHakuaika(a, l))
      case _ => None
    }
  }
}

trait TarjontaService {
  def haku(oid: String, lang: Language.Language) : Option[Haku]
  def hakukohde(oid: String) : Option[Hakukohde]
}