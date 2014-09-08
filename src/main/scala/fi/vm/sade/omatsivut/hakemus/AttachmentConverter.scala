package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.localization.Translations
import fi.vm.sade.omatsivut.domain.Attachment
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import scala.collection.JavaConversions._
import fi.vm.sade.haku.oppija.hakemus.domain.util.ApplicationUtil

case class AttachmentConverter(implicit val appConfig: AppConfig.AppConfig, val language: Language.Language) {

  private val koulutusInformaatio = appConfig.componentRegistry.koulutusInformaatioService

  def getDiscretionaryAttachments(application: Application): List[Attachment] = {
    val heading = Translations.getTranslation("applicationPreview", "discretionary")
    val description = Translations.getTranslation("applicationPreview", "discretionary_info")
    ApplicationUtil.getDiscretionaryAttachmentAOIds(application).toList.map(
        koulutusInformaatio.liitepyynto(_, heading, description)
    )
  }

  def getHigherEdAttachments(application: Application): List[Attachment] = {
    ApplicationUtil
      .getHigherEdAttachmentAOIds(application).mapValues(_.filterNot(_.isEmpty()))
      .flatMap{case (baseEducation, aoIds) => {
        aoIds.map(
            koulutusInformaatio.liitepyynto(_, "", Translations.getTranslation("applicationPreview", "attachments_info_" + baseEducation))
        ).map(info => info.copy(heading = info.providerName.getOrElse("")))
      }}.toList
  }

}