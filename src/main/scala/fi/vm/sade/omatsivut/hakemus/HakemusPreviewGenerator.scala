package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.Phase
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Language

case class HakemusPreviewGenerator(implicit val appConfig: AppConfig, val language: Language.Language) extends Logging {
  import scalatags.Text.all._
  private val applicationDao = appConfig.springContext.applicationDAO
  private val applicationSystemService = appConfig.springContext.applicationSystemService

  def generatePreview(personOid: String, applicationOid: String): Option[String] = {
    import collection.JavaConversions._
    val applicationQuery: Application = new Application().setOid(applicationOid).setPersonOid(personOid)
    applicationDao.find(applicationQuery).toList.headOption.map { application =>
      val applicationSystem = applicationSystemService.getApplicationSystem(application.getApplicationSystemId)
      val form = getFilteredForm(application, applicationSystem)
      val phases = form.getElementsOfType[Phase]
      html(
        body(
          header(
            h1(applicationSystem.getName.getTranslations.get(language.toString))
          ) :: phases.map(phasePreview)
        )
      ).toString
    }
  }

  private def phasePreview(phase: ElementWrapper) = {
    div(`class` := "phase")("VAIHE")
  }

  private def getFilteredForm(application: Application, applicationSystem: ApplicationSystem) = {
    val answers = HakemusConverter.flattenAnswers(ApplicationUpdater.allAnswersFromApplication(application))
    ElementWrapper.wrapFiltered(applicationSystem.getForm, answers)
  }
}
