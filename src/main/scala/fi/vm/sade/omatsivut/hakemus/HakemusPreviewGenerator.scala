package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.SocialSecurityNumber
import fi.vm.sade.haku.oppija.lomake.domain.{I18nText, ApplicationSystem}
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.{PreferenceRow, PreferenceTable, SocialSecurityNumber}
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.gradegrid.{GradeGridAddLang, GradeGridOptionQuestion, GradeGridTitle, GradeGrid}
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{CheckBox, OptionQuestion, TextQuestion, TextArea, DateQuestion}
import fi.vm.sade.haku.oppija.lomake.domain.elements.{TitledGroup, Text, Theme, Phase}
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

    def questionsPreview(element: ElementWrapper): List[TypedTag[String]] = {
      // TODO: PreferenceTable, DateQuestion, DiscretionaryAttachments

      element.element match {
        case _: GradeGrid => List(gradeGridPreview(element))
        case _: PreferenceTable => List(preferenceTablePreview(element))
        case _: TextArea => List(textQuestionPreview(element))
        case _: SocialSecurityNumber => List(textQuestionPreview(element))
        case _: TextQuestion => List(textQuestionPreview(element))
        case _: OptionQuestion => List(optionQuestionPreview(element))
        case _: CheckBox => List(checkBoxPreview(element))
        case _: Theme => List(themePreview(element))
        case _: Phase => flattenedPreview(element)
        case _: RelatedQuestionRule => flattenedPreview(element)
        case _: Text => List(textPreview(element))
        case _: TitledGroup => List(titledGroupPreview(element))
        case _: DateQuestion => List(textPreview(element))
        case _: AddElementRule => Nil
        case _ =>
          logger.warn("Ignoring element " + element.element.getType + ": " + element.id)
          Nil
      }
    }

    def themePreview(element: ElementWrapper): TypedTag[String] = {
      div(`class` := "theme")(h2(element.title), flattenedPreview(element))
    }

    def flattenedPreview(element: ElementWrapper) = {
      element.children.flatMap(questionsPreview)
    }

    def questionPreview(question: String, answer: String) = {
      div(`class` := "question")(
        label(question),
        span(`class` := "answer")(answer)
      )
    }

    def textQuestionPreview(element: ElementWrapper) = {
      questionPreview(element.title, answers.get(element.id).getOrElse("").asInstanceOf[String])
    }

    def optionQuestionPreview(element: ElementWrapper) = {
      val answer = answerFromOptions(element.options, element.id)
      questionPreview(element.title, answer)
    }

    def answerFromOptions(options: List[OptionWrapper], key: String) = {
      options
        .find { option => Some(option.value) == answers.get(key)}
        .map { option => option.title }
        .getOrElse("")
    }

    def checkBoxPreview(element: ElementWrapper) = {
      val answer: Option[String] = answers.get(element.id)
      val checkBoxValue = element.element.asInstanceOf[CheckBox].getValue
      val checked = (answer == Some(checkBoxValue))
      val translatedAnswer = checked match {
        case true => "Kyllä" // TODO: kielistys
        case false => "Ei"
      }
      questionPreview(element.title, translatedAnswer)
    }

    def titledGroupPreview(element: ElementWrapper) = {
      div(`class` := "group")(
        h3(element.title) :: element.children.flatMap(questionsPreview)
      )
    }

    def preferenceTablePreview(tableElement: ElementWrapper) = {
      div(`class` := "preference-table")(
        tableElement.children.zipWithIndex.flatMap { case (childElement, index) =>
          childElement.element match {
            case row: PreferenceRow =>
              List(div(`class` := "preference-row")(
                span(`class` := "index")(index+1),
                span(`class` := "opetuspiste")(
                  label(ElementWrapper.t(row.getLearningInstitutionLabel)),
                  span("TODO")
                )
              ))
            case _ =>
              logger.warn("Ignoring preference table element " + childElement.element.getType + ": " + childElement.id)
              Nil
          }
        }
      )
    }

    def gradeGridPreview(gridElement: ElementWrapper) = {
      table(`class` := "gradegrid")(
        thead(
          td("Oppiaine") :: td() :: (if (gridElement.element.asInstanceOf[GradeGrid].isExtraColumn) {
            List(td("Yhteinen oppiaine"), td("Valinnainen aine"), td("Toinen valinnainen aine"))
          } else {
            List(td("Arvosana"))
          })
        ),
        tbody(gridElement.children.map { row =>
          tr(row.children.map { column =>
            td(
              column.children.map { dataElement =>
                gradeGridElementPreview(answerFromOptions _, dataElement)
              }
            )
          }
          )
        })
      )
    }

    def gradeGridElementPreview(answerFromOptions: (List[OptionWrapper], String) => String, dataElement: ElementWrapper): List[String] = {
      dataElement.element match {
        case _: GradeGridTitle => List(dataElement.title)
        case _: GradeGridOptionQuestion =>
          val gradeValue = answerFromOptions(dataElement.options, dataElement.id)
          val kymppiValue = answerFromOptions(dataElement.options, dataElement.id + "_10")
          List(kymppiValue match {
            case "" => gradeValue
            case value => value + "(" + gradeValue + ")"
          })
        case _: GradeGridAddLang => Nil
        case _ =>
          logger.warn("Ignoring grade grid element " + dataElement.element.getType + ": " + dataElement.id)
          Nil
      }
    }

    def textPreview(element: ElementWrapper) = {
      div(`class` := "text")(element.title)
    }
    html(
      head(link(href := "/omatsivut/css/preview.css", rel:= "stylesheet", `type` := "text/css")),
      body(
        header(
          h1(ElementWrapper.t(applicationSystem.getName))
        )
        ::
        form.getElementsOfType[Phase].flatMap(questionsPreview)
      )
    ).toString
  }
}
