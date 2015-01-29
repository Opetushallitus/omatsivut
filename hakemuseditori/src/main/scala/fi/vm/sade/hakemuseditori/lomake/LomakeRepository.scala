package fi.vm.sade.hakemuseditori.lomake

import com.google.common.util.concurrent.UncheckedExecutionException
import fi.vm.sade.hakemuseditori.hakemus.{ImmutableLegacyApplicationWrapper, SpringContextComponent}
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.exception.ApplicationSystemNotFound
import fi.vm.sade.haku.oppija.lomake.service.impl.ApplicationSystemServiceImpl
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.hakemuseditori.lomake.domain.Lomake
import fi.vm.sade.hakemuseditori.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakuaika}
import fi.vm.sade.hakemuseditori.tarjonta.{TarjontaComponent, TarjontaService}
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
        case hakuOid => (tryFind(hakuOid), tarjontaService.haku(hakuOid, lang)) // Miksi nämä bundlattu?
      }
    }

    private def tryFind(applicationSystemOid: String): Option[Lomake] = {
      try {
        reportCacheStats()
        Some(timed("Application system service", 1000){
          val applicationSystem: ApplicationSystem = repository.getApplicationSystem(applicationSystemOid)
          Lomake(applicationSystem)
        })
      } catch {
        case e: UncheckedExecutionException if (e.getCause.isInstanceOf[ApplicationSystemNotFound]) =>
          None
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
