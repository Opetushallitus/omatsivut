package fi.vm.sade.hakemuseditori.tarjonta

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.fixtures.JsonFixtureMaps
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.memoize.TTLOptionalMemoize
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde, KohteenHakuaika}
import fi.vm.sade.hakemuseditori.tarjonta.vanha.RemoteTarjontaComponent
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.JsonAST.JValue

import scala.collection.mutable

trait TarjontaComponent {
  this: OhjausparametritComponent
    with RemoteTarjontaComponent =>

  val tarjontaService: TarjontaService

  class StubbedTarjontaService extends TarjontaService with JsonFormats with Logging {
    private val timeOverrides = mutable.Map[String, Long]()
    private val hakukierrospaattyyOverrides = mutable.Map[String, Long]()
    private val priorities = mutable.Set[String]()

    private def parseHaku(oid: String, lang: Language.Language) = {
      JsonFixtureMaps.findByKey[JValue]("/hakemuseditorimockdata/haut.json", oid).flatMap(TarjontaParser.parseHaku).map {h => Haku(h, lang)}
    }

    override def haku(oid: String, lang: Language.Language) = {
      val haku = parseHaku(oid, lang)
      if(haku.isEmpty) {
        logger.error("No haku data for " + oid)
      }
      haku.map { h =>
        h.copy(usePriority = if(priorities.contains(oid)) !h.usePriority else h.usePriority)
      }.map {h =>
        val haunAikataulu = ohjausparametritService.haunAikataulu(oid)
        if(timeOverrides.contains(oid)) {
          h.copy(applicationPeriods = changeHakuajat(h), aikataulu = changeAikataulu(h, haunAikataulu))
        } else {
          h.copy(aikataulu = haunAikataulu)
        }
      }
    }

    override def hakukohde(oid: String): Option[Hakukohde] = {
      val json = JsonFixtureMaps.findByKey[JValue]("/hakemuseditorimockdata/hakukohteet.json", oid)
      json.flatMap(TarjontaParser.parseHakukohde).map { hakukohde =>
        val hakuOid = json match {
          case Some(v) => (v \ "result" \ "hakuOid").extract[String]
          case _ => ""
        }
        if(timeOverrides.contains(hakuOid)){
          hakukohde.copy(kohteenHakuaika = hakukohde.kohteenHakuaika.map { aika =>
            val haku : Option[Haku] = parseHaku(hakuOid, Language.fi)
            changeKohteenHakuaika(haku, aika)
          })
        } else {
          hakukohde
        }
      }
    }

    private def changeKohteenHakuaika(haku: Option[Haku], aika: KohteenHakuaika): KohteenHakuaika = {
      haku match {
        case Some(h) => KohteenHakuaika(changeTimestamp(h, aika.start), changeTimestamp(h, aika.end))
        case _ => aika
      }
    }

    def resetHaunAlkuaika(hakuOid: String): Unit = {
      timeOverrides -= hakuOid
    }

    def resetHakukierrosPaattyy(hakuOid: String): Unit = {
      hakukierrospaattyyOverrides -= hakuOid
    }


    def modifyHaunAlkuaika(hakuOid: String, alkuaika: Long) {
      timeOverrides.put(hakuOid, alkuaika)
    }

    def modifyHakukierrosPaattyy(hakuOid: String, hakukierrospaattyy: Long) {
      hakukierrospaattyyOverrides.put(hakuOid, hakukierrospaattyy)
    }

    def resetPriority(hakuOid: String): Unit = {
      priorities -= hakuOid
    }

    def invertPriority(hakuOid: String): Unit = {
      priorities.add(hakuOid)
    }

    private def changeHakuajat(haku: Haku) = {
      haku.applicationPeriods.map { aika =>
        aika.copy(start = changeTimestamp(haku, aika.start), end = changeTimestamp(haku, aika.end))
      }
    }

    private def changeAikataulu(haku: Haku, aikataulu: Option[HaunAikataulu]) = {
      if(hakukierrospaattyyOverrides.contains(haku.oid)) {
        Some(HaunAikataulu(aikataulu.get.julkistus, hakukierrospaattyyOverrides.get(haku.oid)))
      } else {
        aikataulu
      }
    }

    private def changeTimestamp(haku: Haku, timestamp: Long) : Long = {
      val start = timeOverrides(haku.oid)
      val originalStart = haku.applicationPeriods.minBy(_.start).start
      start + (timestamp - originalStart)
    }

  }

  object CachedRemoteTarjontaService {
    def apply(): TarjontaService = {
      val service = new RemoteTarjontaService()
      val hakuMemo = TTLOptionalMemoize.memoize(service.haku _, "tarjonta haku", 4 * 60 * 60, 128)
      val hakukohdeMemo = TTLOptionalMemoize.memoize(service.hakukohde _, "tarjonta hakukohde", 4 * 60 * 60, 1024)

      new TarjontaService {
        override def haku(oid: String, lang: Language): Option[Haku] = hakuMemo(oid, lang)
        override def hakukohde(oid: String): Option[Hakukohde] = hakukohdeMemo(oid)
      }
    }
  }
}


