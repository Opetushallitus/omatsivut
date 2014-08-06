package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Language

case class HakemusPreviewGenerator(implicit val appConfig: AppConfig, val language: Language.Language) extends Logging {
  private val applicationDao = appConfig.springContext.applicationDAO
  private val applicationSystemService = appConfig.springContext.applicationSystemService

  def generatePreview(personOid: String, applicationOid: String): Option[String] = {
    import collection.JavaConversions._
    import scalatags.Text.all._
    val applicationQuery: Application = new Application().setOid(applicationOid).setPersonOid(personOid)
    applicationDao.find(applicationQuery).toList.headOption.map { application =>
      val applicationSystem = applicationSystemService.getApplicationSystem(application.getApplicationSystemId)
      getFilteredForm(application, applicationSystem)
      html(
        body(
          header(
            h1(applicationSystem.getName.getTranslations.get(language.toString))
          )
        )
      ).toString
    }
  }

  private def getFilteredForm(application: Application, applicationSystem: ApplicationSystem) = {
    val answers = HakemusConverter.flattenAnswers(ApplicationUpdater.allAnswersFromApplication(application))
    ElementWrapper.wrapFiltered(applicationSystem.getForm, answers)
  }
}
