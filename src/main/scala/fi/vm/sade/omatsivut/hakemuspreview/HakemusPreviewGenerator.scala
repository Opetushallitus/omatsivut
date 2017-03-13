package fi.vm.sade.omatsivut.hakemuspreview

import java.text.SimpleDateFormat
import java.util.Date

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.domain.{Address, Attachment, Language}
import fi.vm.sade.hakemuseditori.hakemus.FlatAnswers.FlatAnswers
import fi.vm.sade.hakemuseditori.hakemus._
import fi.vm.sade.hakemuseditori.localization.{Translations, TranslationsComponent}
import fi.vm.sade.hakemuseditori.lomake.{ElementWrapper, FilteredElementWrapper, OptionWrapper}
import fi.vm.sade.haku.oppija.hakemus.domain.ApplicationAttachment
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.oppija.lomake.domain.elements._
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.gradegrid.{GradeGrid, GradeGridAddLang, GradeGridOptionQuestion, GradeGridTitle}
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.{GradeAverage, PostalCode, PreferenceRow, PreferenceTable, SocialSecurityNumber}
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{CheckBox, DateQuestion, OptionQuestion, TextArea, TextQuestion}
import fi.vm.sade.haku.oppija.lomake.domain.rules.{AddElementRule, RelatedQuestionRule}
import fi.vm.sade.utils.slf4j.Logging

import scalatags.Text.TypedTag
import scalatags.Text.all._
import org.apache.commons.lang.StringEscapeUtils;

trait HakemusPreviewGeneratorComponent {
  this: SpringContextComponent with HakemusRepositoryComponent with HakemusConverterComponent with TranslationsComponent =>

  def newHakemusPreviewGenerator(language: Language.Language): HakemusPreviewGenerator

  class HakemusPreviewGenerator(language: Language.Language) extends Logging {
    private val applicationSystemService = springContext.applicationSystemService

    def generatePreview(personOid: String, applicationOid: String): Option[String] = {
      implicit val (t, lang) = (translations, language)
      applicationRepository.findStoredApplicationByPersonAndOid(personOid, applicationOid).map { application =>
        val applicationSystem = applicationSystemService.getApplicationSystem(application.hakuOid)
        HakemusPreview().generate(application, applicationSystem)
      }
    }
  }
}

case class HakemusPreview(implicit translations: Translations, language: Language) extends Logging {
  import scala.collection.JavaConversions._
  import scalatags.Text.all._

  def generate(application: ImmutableLegacyApplicationWrapper, applicationSystem: ApplicationSystem): String = {
    val form = ElementWrapper.wrapFiltered(applicationSystem.getForm, application.flatAnswers)
    val addInfos: List[FilteredElementWrapper] = applicationSystem.getAdditionalInformationElements.toList.map { addInfo =>
      ElementWrapper.wrapFiltered(addInfo, application.flatAnswers)
    }

    """<!DOCTYPE html>""" +
      html(
        head(link(href := "/omatsivut/css/preview.css", rel:= "stylesheet", `type` := "text/css")),
        body(
          header(
            h1(ElementWrapper.t(applicationSystem.getName)),
            h2(application.flatAnswers.get("Etunimet").getOrElse("") + " " + application.flatAnswers.get("Sukunimi").getOrElse("")),
            div(`class` := "detail application-received")(label(translations.getTranslation("applicationPreview", "received")), span(DateFormat.formatDate(application.received))),
            div(`class` := "detail application-id")(label(translations.getTranslation("applicationPreview", "applicationId")), span(formatOid(application.oid)))
          )
            ::
            form.getElementsOfType[Phase].flatMap(QuestionsPreview().generate(_, application.flatAnswers, true))
            :::
            AdditionalInformationPreview().generate(addInfos, application.flatAnswers)
            :::
            List(a(name := "liitteet"))
            :::
            AttachmentsPreview().generate(application.attachments)
          ,
          script(`type` := "text/javascript", src := "/omatsivut/piwik/load")
        )
      )
  }

  def formatOid(oid: String): String = {
    val split = oid.split("\\.").toList
    split match {
      case Nil => ""
      case parts => parts.last
    }
  }
}

case class QuestionsPreview(implicit translations: Translations, language: Language) extends Logging {
  import scala.collection.JavaConversions._
  import scalatags.Text.all._

  def generate(ew: ElementWrapper, answers: FlatAnswers, showEmptyValues: Boolean): List[TypedTag[String]] = {
    ew.element match {
      case e: GradeGrid             => List(gradeGridPreview(ew, answers))
      case e: PreferenceTable       => List(preferenceTablePreview(ew, answers))
      case e: PostalCode            => postalCodePreview(ew, answers)
      case e: OptionQuestion        => optionQuestionPreview(ew, answers)
      case e: CheckBox              => checkBoxPreview(ew, answers)
      case e: Theme                 => themePreview(ew, answers)
      case e: Phase                 => childrenPreview(ew, answers)
      case e: RelatedQuestionRule   => childrenPreview(ew, answers)
      case e: GradeAverage          => childrenPreview(ew, answers)
      case e: Text                  => List(textPreview(ew))
      case e: Notification          => List(textPreview(ew))
      case e: Link                  => List(linkPreview(ew, e))
      case e: TitledGroup           => List(titledGroupPreview(ew, answers))
      case e: AddElementRule        => childrenPreview(ew, answers, false)
      case e: HiddenValue           => Nil // info about attachments are added separately in the end of the document
      case e: TextArea              => textQuestionPreview(ew, answers)
      case e: SocialSecurityNumber  => textQuestionPreview(ew, answers)
      case e: TextQuestion          => textQuestionPreview(ew, answers, showEmptyValues)
      case e: DateQuestion          => textQuestionPreview(ew, answers)
      case e: RichText              => List(richTextPreview(ew))
      case e =>
        logger.warn("Ignoring element " + e.getType + ": " + e.getId)
        Nil
    }
  }

  def questionPreview(question: String, answer: String, showEmptyValues: Boolean = true) = {
    if(answer.trim().isEmpty() && !showEmptyValues) {
      Nil
    }
    else {
      div(`class` := "question")(
        previewLabel(question),
        span(`class` := "answer")(answer)
      ) :: Nil
    }
  }

  def previewLabel(question: String) = {
    if(question.isEmpty) {
      label(raw("&nbsp;"))
    }
    else {
      label(raw(question))
    }
  }

  def textQuestionPreview(element: ElementWrapper, answers: FlatAnswers, showEmptyValues: Boolean = true) = {
    questionPreview(element.title, answers.get(element.id).getOrElse("").asInstanceOf[String], showEmptyValues)
  }

  def postalCodePreview(element: ElementWrapper, answers: FlatAnswers) = {
    val answer = selectedOption(element.options, element.id, answers)
      .map { option => option.value + " " + option.title }
      .getOrElse("")
    questionPreview("", answer) ::: childrenPreview(element, answers)
  }

  def optionQuestionPreview(element: ElementWrapper, answers: FlatAnswers) = {
    val answer = answerFromOptions(element.options, element.id, answers)
    questionPreview(element.title, answer) ::: childrenPreview(element, answers)
  }

  def selectedOption(options: List[OptionWrapper], key: String, answers: FlatAnswers) = {
    options
      .find { option => Some(option.value) == answers.get(key)}
  }

  def answerFromOptions(options: List[OptionWrapper], key: String, answers: FlatAnswers) = {
    selectedOption(options, key, answers)
      .map { option => option.title }
      .getOrElse("")
  }

  def checkBoxPreview(element: ElementWrapper, answers: FlatAnswers) = {
    val answer: Option[String] = answers.get(element.id)
    val checkBoxValue = element.element.asInstanceOf[CheckBox].getValue
    val checked = (answer == Some(checkBoxValue))
    val translatedAnswer = checked match {
      case true => translations.getTranslation("message", "yes")
      case false => translations.getTranslation("message", "no")
    }
    questionPreview(element.title, translatedAnswer) ::: childrenPreview(element, answers)
  }

  def titledGroupPreview(element: ElementWrapper, answers: FlatAnswers) = {
    div(`class` := "group")(
      h3(raw(element.title)) :: childrenPreview(element, answers)
    )
  }

  def preferenceTablePreview(tableElement: ElementWrapper, answers: FlatAnswers) = {
    ul(`class` := "preference-table")(
      tableElement.children.zipWithIndex.flatMap { case (childElement, index) =>
        childElement.element match {
          case row: PreferenceRow =>
            answers.getOrElse(row.getEducationOidInputId, "") match {
              case null => Nil
              case "" => Nil
              case _ => List(li(`class` := "preference-row")(
                List(
                  span(`class` := "index")(index+1),
                  span(`class` := "learning-institution")(
                    label(ElementWrapper.t(row.getLearningInstitutionLabel)),
                    span(answers(row.getLearningInstitutionInputId))
                  ),
                  span(`class` := "education")(
                    label(ElementWrapper.t(row.getEducationLabel)),
                    span(answers(row.getEducationInputId))
                  )
                ) ++ preferenceQuestionsPreview(childElement, answers)
              ))
            }
          case _ =>
            logger.warn("Ignoring preference table element " + childElement.element.getType + ": " + childElement.id)
            Nil
        }
      }
    )
  }

  def preferenceQuestionsPreview(element: ElementWrapper, answers: FlatAnswers) = {
    childrenPreview(element, answers).toList match {
      case Nil =>
        Nil
      case children =>
        List(div(`class` := "questions")(h3(translations.getTranslation("applicationPreview", "preferences_questions")), children))
    }
  }

  def themePreview(element: ElementWrapper, answers: FlatAnswers): List[TypedTag[String]] = {
    if(element.children.isEmpty) {
      Nil
    }
    else {
      List(div(`class` := "theme")(h2(raw(element.title)), childrenPreview(element, answers)))
    }
  }

  def childrenPreview(element: ElementWrapper, answers: FlatAnswers, showEmptyValues: Boolean = true) = {
    element.children.flatMap(generate(_, answers, showEmptyValues))
  }

  def gradeGridPreview(gridElement: ElementWrapper, answers: FlatAnswers) = {
    table(`class` := "gradegrid")(
      thead(
        td("colspan".attr := 2)(translations.getTranslation("applicationPreview", "subject")) :: (if (gridElement.element.asInstanceOf[GradeGrid].isExtraColumn) {
          List(td(translations.getTranslation("applicationPreview", "mutual_subject")), td(translations.getTranslation("applicationPreview", "optional_subject")), td(translations.getTranslation("applicationPreview", "second_optional_subject")))
        } else {
          List(td(translations.getTranslation("applicationPreview", "grade")))
        })
      ),
      tbody(gridElement.children.map { row =>
        tr(row.children.map { column =>
          td(attrs(column))( // <- practically there's a colspan attribute that needs to be set
            column.children.map { dataElement =>
              gradeGridElementPreview({case (options: List[OptionWrapper], key: String) => answerFromOptions(options, key, answers)}, dataElement)
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

  def richTextPreview(element: ElementWrapper) = {
    div(`class` := "text")(raw(StringEscapeUtils.unescapeHtml(element.title)))
  }

  def linkPreview(element: ElementWrapper, link: Link) = {
    a(`href` := ElementWrapper.t(link.getUrl()), `target` := "_blank")(element.title)
  }

  def attrs(el: ElementWrapper): List[Modifier] = {
    el.element.getAttributes.toMap.map { case (key, value) =>
      key.attr := value
    }.toList
  }
}

case class AdditionalInformationPreview(implicit translations: Translations, language: Language) extends Logging {
  def generate(addInfos: List[FilteredElementWrapper], answers: FlatAnswers): List[TypedTag[String]] = {
    (for(addInfo <- addInfos) yield additionalInformationElementPreview(addInfo, answers)
      ).flatten.toList
  }

  def additionalInformationElementPreview(element: ElementWrapper, answers: FlatAnswers): List[TypedTag[String]] = {
    if(element.children.isEmpty) {
      Nil
    }
    else {
      List(div(`class` := "theme")(
        hr(),
        QuestionsPreview().generate(element, answers, true)
      ))
    }
  }
}

case class AttachmentsPreview(implicit translations: Translations, language: Language) extends Logging {
  def generate(attachments: List[ApplicationAttachment]): List[TypedTag[String]] = {
    val aoInfos = attachments.map(AttachmentConverter.convertToAttachment(_))

    if (aoInfos.isEmpty) {
      Nil
    }
    else {
      List(div(`class` := "theme")(
        h2(translations.getTranslation("applicationPreview", "attachments")),
        table(`class` := "striped") (
          tr(
            th(translations.getTranslation("applicationPreview", "attachment")),
            th(translations.getTranslation("applicationPreview", "attachmentAddress")),
            th(translations.getTranslation("applicationPreview", "attachmentDeadline"))
          ),
          for (info <- aoInfos) yield attachmentInfoPreview(info))
      )
      )
    }
  }

  private def attachmentInfoPreview(info: Attachment): List[TypedTag[String]] = {
    List(tr(
      td(
        elemIfNotEmptyString(p(_), info.name),
        elemIfNotEmptyString(text => p(text), info.heading),
        elemIfNotEmptyString(text => div(raw(text)), info.description)
      ),
      td(elemIfNotEmptyString(div(_), info.recipientName ),
        attachmentAddressInfoPreview(info.address),
        elemIfNotEmptyString(div(_), info.emailAddress )
      ),
      td(elemIfNotEmpty[Long](div(_), info.deadline, formatDeadlinePreview))

    ))
  }

  private def attachmentAddressInfoPreview(address: Option[Address]): List[TypedTag[String]] = {
    if(address.isDefined) {
      List(
        elemIfNotEmptyString(div(_), address.get.streetAddress),
        elemIfNotEmptyString(div(_), address.get.streetAddress2),
        elemIfNotEmptyString(div(_), address.get.postalCode),
        elemIfNotEmptyString(div(_), address.get.postOffice )
      ).flatten
    }
    else {
      Nil
    }
  }


  private def formatDeadlinePreview(date: Long): String = {
    DateFormat.formatDate(Some(new Date(date)))
  }

  private def elemIfNotEmptyString(elem: String => TypedTag[String], value: Option[String]): Option[TypedTag[String]] = {
    elemIfNotEmpty[String](elem, value, _.toString())
  }

  private def elemIfNotEmpty[T](elem: String => TypedTag[String], value: Option[T], format: T => String): Option[TypedTag[String]] = {
    if (value.isDefined && format(value.get).trim().length() > 0) {
      Some(elem(format(value.get).trim()))
    } else {
      None
    }
  }
}

object DateFormat {
  def formatDate(date: Option[Date]) = date match {
    case Some(date) => new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date)
    case None => ""
  }
}
