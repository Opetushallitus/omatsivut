package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.gradegrid.GradeGrid
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{OptionQuestion, TextQuestion, TextArea}
import fi.vm.sade.haku.oppija.lomake.domain.elements.{Text, Theme, Phase}
import fi.vm.sade.haku.oppija.lomake.domain.rules.{AddElementRule, RelatedQuestionRule}
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.HakemusConverter.FlatAnswers

import scalatags.Text.TypedTag

case class HakemusPreviewGenerator(implicit val appConfig: AppConfig, val language: Language.Language) extends Logging {
  import scalatags.Text.all._
  import collection.JavaConversions._
  private val applicationDao = appConfig.springContext.applicationDAO
  private val applicationSystemService = appConfig.springContext.applicationSystemService

  def generatePreview(personOid: String, applicationOid: String): Option[String] = {
    val applicationQuery: Application = new Application().setOid(applicationOid).setPersonOid(personOid)
    applicationDao.find(applicationQuery).toList.headOption.map { application =>
      applicationPreview(application)
    }
  }

  private def applicationPreview(application: Application) = {
    val applicationSystem = applicationSystemService.getApplicationSystem(application.getApplicationSystemId)
    val answers: FlatAnswers = HakemusConverter.flattenAnswers(ApplicationUpdater.allAnswersFromApplication(application))
    val form = ElementWrapper.wrapFiltered(applicationSystem.getForm, answers)

    def themePreview(phase: ElementWrapper): TypedTag[String] = {
      div(`class` := "theme")(phase.title, phase.children.flatMap(questionsPreview))
    }

    def questionsPreview(element: ElementWrapper): List[TypedTag[String]] = {
      element.element match {
        case g: GradeGrid => List(div(`class` := "grid")) // <- todo test data missing (try peruskoulu fixture)
        case t: TextArea => List(textQuestionPreview(element))
        case t: SocialSecurityNumber => List(textQuestionPreview(element))
        case t: TextQuestion => List(textQuestionPreview(element))
        case t: OptionQuestion => List(optionQuestionPreview(element))
        case r: RelatedQuestionRule => element.children.flatMap(questionsPreview)
        case r: Text => List(textPreview(element))
        case r: AddElementRule => Nil
        case _ =>
          logger.warn("Ignoring element " + element.element.getType + ": " + element.id)
          Nil
      }
    }

    def questionPreview(question: String, answer: String) = {
      div(`class` := "question")(
        label(question),
        span(answer)
      )
    }

    def textQuestionPreview(element: ElementWrapper) = {
      questionPreview(element.title, answers.get(element.id).getOrElse("").asInstanceOf[String])
    }

    def optionQuestionPreview(element: ElementWrapper) = {
      val answer = element.options
        .find { option => Some(option.value) == answers.get(element.id)}
        .map { option => option.title }
        .getOrElse("")
      questionPreview(element.title, answer)
    }

    def textPreview(element: ElementWrapper) = {
      div(`class` := "text")(element.title)
    }

    html(
      body(
        header(
          h1(applicationSystem.getName.getTranslations.get(language.toString))
        ) :: form.getElementsOfType[Theme].map(themePreview)
      )
    ).toString
  }
}
