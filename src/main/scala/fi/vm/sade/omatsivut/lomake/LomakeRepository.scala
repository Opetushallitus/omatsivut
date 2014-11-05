package fi.vm.sade.omatsivut.lomake

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.service.impl.ApplicationSystemServiceImpl
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.lomake.domain.Lomake
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.tarjonta.{Haku, Hakuaika, TarjontaComponent, TarjontaService}
import fi.vm.sade.omatsivut.util.Logging
import fi.vm.sade.omatsivut.util.Timer.timed
import org.joda.time.DateTime

trait LomakeRepositoryComponent {
  this: OhjausparametritComponent with KoulutusInformaatioComponent with SpringContextComponent with TarjontaComponent =>

  val lomakeRepository: LomakeRepository
  val ohjausparametritService: OhjausparametritService
  val koulutusInformaatioService: KoulutusInformaatioService
  val tarjontaService: TarjontaService

  class RemoteLomakeRepository extends LomakeRepository with Logging {
    private val repository = springContext.applicationSystemService
    private[this] var lastReport = new DateTime()

    def lomakeByOid(oid: String): Option[Lomake] = {
      tryFind(oid)
    }

    def lomakeAndHakuByApplication(application: Application)(implicit lang: Language.Language): (Option[Lomake], Option[Haku]) = {
      application.getApplicationSystemId match {
        case "" => (None, None)
        case applicationSystemId => (tryFind(applicationSystemId), tarjontaService.haku(applicationSystemId, lang))
      }
    }

    private def tryFind(applicationSystemOid: String): Option[Lomake] = {
      try {
        reportCacheStats()
        Some(timed(1000, "Application system service"){
          Lomake(repository.getApplicationSystem(applicationSystemOid))
        })
      } catch {
        case e: Exception =>
          logger.error("applicationSystem loading failed", e)
          None
      }
    }

    def applicationPeriodsByOid(applicationSystemId: String)(implicit lang: Language.Language) : List[Hakuaika] = {
      tarjontaService.haku(applicationSystemId, lang).map(h => h.applicationPeriods) match {
        case Some(hakuajat) => hakuajat
        case _ => List()
      }
    }

    def reportCacheStats(): Unit = {
      if (lastReport.plusHours(1).isBeforeNow) {
        lastReport.synchronized {
          lastReport = new DateTime()
        }
        logger.info(s"Reporting hourly cache stats on lomake repository cache: " + repository.asInstanceOf[ApplicationSystemServiceImpl].getCacheStats().toString)
      }
    }
  }
}

trait LomakeRepository {
  def lomakeByOid(oid: String): Option[Lomake]
  def lomakeAndHakuByApplication(application: Application)(implicit lang: Language.Language): (Option[Lomake], Option[Haku])
  def applicationPeriodsByOid(applicationSystemId: String)(implicit lang: Language.Language) : List[Hakuaika]
}
