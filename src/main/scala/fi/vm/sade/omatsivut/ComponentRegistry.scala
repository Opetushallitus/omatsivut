package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.{AppConfig, StubbedExternalDeps}
import fi.vm.sade.omatsivut.haku.{HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}

object ComponentRegistry extends KoulutusInformaatioComponent with OhjausparametritComponent with HakuRepositoryComponent {
  implicit lazy val config: AppConfig = AppConfig.fromSystemProperty

  private def configureOhjausparametritService: OhjausparametritService = config match {
    case _ : StubbedExternalDeps => new StubbedOhjausparametritService()
    case _ => CachedRemoteOhjausparametritService(config)
  }

  private def configureKoulutusInformaatioService: KoulutusInformaatioService = config match {
    case x: StubbedExternalDeps => new StubbedKoulutusInformaatioService
    case _ => CachedKoulutusInformaatioService(config)
  }

  override val koulutusInformaatioService: KoulutusInformaatioService = configureKoulutusInformaatioService
  override val ohjausparametritService: OhjausparametritService = configureOhjausparametritService
  val hakuRepository: HakuRepository = new RemoteHakuRepository()
}
