package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.koulutusinformaatio.Liitepyynto
import fi.vm.sade.omatsivut.AppConfig
import fi.vm.sade.haku.oppija.hakemus.domain.util.ApplicationUtil
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import fi.vm.sade.omatsivut.Logging

protected object AddedLiitepyyntoFinder extends Logging {
  import scala.collection.JavaConversions._
  def findDiscretionaryLiitepyynnot(applicationSystem: ApplicationSystem, storedApplication: Application, hakemus: Hakemus)(implicit appConfig: AppConfig.AppConfig, lang: Language.Language): List[Liitepyynto] = {
    val oldLiitepyynnot = ApplicationUtil.getDiscretionaryAttachmentAOIds(storedApplication).toList.filter(_.nonEmpty)
    val newLiitepyynnot = ApplicationUtil.getDiscretionaryAttachmentAOIds(ApplicationUpdater.update(applicationSystem)(storedApplication.clone(), hakemus)).toList.filter(_.nonEmpty)
    val koulutusinformaatio = KoulutusInformaatioService.apply
    newLiitepyynnot.diff(oldLiitepyynnot).map(koulutusinformaatio.liitepyynto(_))
  }

  def findHigherDegreeLiitepyynnot(applicationSystem: ApplicationSystem, storedApplication: Application, hakemus: Hakemus)(implicit appConfig: AppConfig.AppConfig, lang: Language.Language): Map[String,List[Liitepyynto]] = {
    val oldLiitepyynnot = ApplicationUtil.getHigherEdAttachmentAOIds(storedApplication).mapValues(_.toList.filter(_.nonEmpty)).toMap.filter(_._2.nonEmpty)
    val newLiitepyynnot = ApplicationUtil.getHigherEdAttachmentAOIds(ApplicationUpdater.update(applicationSystem)(storedApplication.clone(), hakemus)).mapValues(_.toList.filter(_.nonEmpty)).toMap.filter(_._2.nonEmpty)
    val koulutusinformaatio = KoulutusInformaatioService.apply
    newLiitepyynnot.map(tuple => (tuple._1, tuple._2.diff(oldLiitepyynnot.getOrElse(tuple._1, List())).map(koulutusinformaatio.liitepyynto(_)))).filter(_._2.nonEmpty)
  }

}
