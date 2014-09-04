package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.AppConfig.{AppConfig, ITWithSijoitteluService, StubbedExternalDeps}
import fi.vm.sade.omatsivut.hakemus.{HakemusRepository, HakemusRepositoryComponent}
import fi.vm.sade.omatsivut.haku.{HakuRepository, HakuRepositoryComponent}
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.omatsivut.ohjausparametrit.{OhjausparametritComponent, OhjausparametritService}
import fi.vm.sade.omatsivut.valintatulokset.{ValintatulosService, ValintatulosServiceComponent}

class ComponentRegistry(implicit val config: AppConfig) extends KoulutusInformaatioComponent with
                                OhjausparametritComponent with
                                HakuRepositoryComponent with
                                HakemusRepositoryComponent with
                                ValintatulosServiceComponent {

  private def configureOhjausparametritService: OhjausparametritService = config match {
    case _ : StubbedExternalDeps => new StubbedOhjausparametritService()
    case _ => CachedRemoteOhjausparametritService(config)
  }

  private def configureKoulutusInformaatioService: KoulutusInformaatioService = config match {
    case x: StubbedExternalDeps => new StubbedKoulutusInformaatioService
    case _ => CachedKoulutusInformaatioService(config)
  }

  private def configureValintatulosService: ValintatulosService = config match {
    case x: ITWithSijoitteluService =>
      new RemoteValintatulosService("http://localhost:8180/resources/sijoittelu") {
        override def makeRequest(url: String) =  {
          super.makeRequest(url).map(_.header("Authorization", "Basic " + System.getProperty("omatsivut.sijoittelu.auth")))
        }
      }
    case x: StubbedExternalDeps =>
      new MockValintatulosService()
    case _ =>
      new NoOpValintatulosService
  }

  override val koulutusInformaatioService: KoulutusInformaatioService = configureKoulutusInformaatioService
  override val ohjausparametritService: OhjausparametritService = configureOhjausparametritService
  override val valintatulosService: ValintatulosService = configureValintatulosService
  val hakuRepository: HakuRepository = new RemoteHakuRepository()
  val hakemusRepository: HakemusRepository = new RemoteHakemusRepository()
}
