package fi.vm.sade.omatsivut.lomake

import fi.vm.sade.haku.oppija.lomake.service.impl.ApplicationSystemServiceImpl
import fi.vm.sade.omatsivut.config.SpringContextComponent
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.ImmutableLegacyApplicationWrapper
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.lomake.domain.Lomake
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.tarjonta.domain.{Haku, Hakuaika}
import fi.vm.sade.omatsivut.tarjonta.{TarjontaComponent, TarjontaService}
import fi.vm.sade.utils.slf4j.Logging
import fi.vm.sade.utils.Timer.timed
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

    def lomakeAndHakuByApplication(application: ImmutableLegacyApplicationWrapper)(implicit lang: Language.Language): (Option[Lomake], Option[Haku]) = {
      application.hakuOid match {
        case "" => (None, None)
        case hakuOid => (tryFind(hakuOid), tarjontaService.haku(hakuOid, lang))
      }
    }

    private def tryFind(applicationSystemOid: String): Option[Lomake] = {
      try {
        reportCacheStats()
        Some(timed("Application system service", 1000){
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
  def lomakeAndHakuByApplication(application: ImmutableLegacyApplicationWrapper)(implicit lang: Language.Language): (Option[Lomake], Option[Haku])
  def applicationPeriodsByOid(applicationSystemId: String)(implicit lang: Language.Language) : List[Hakuaika]
}
