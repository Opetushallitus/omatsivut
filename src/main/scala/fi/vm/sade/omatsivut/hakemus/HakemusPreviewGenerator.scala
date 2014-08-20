package fi.vm.sade.omatsivut.hakemus

import java.text.SimpleDateFormat
import java.util.Date
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.gradegrid.{GradeGrid, GradeGridAddLang, GradeGridOptionQuestion, GradeGridTitle}
import fi.vm.sade.haku.oppija.lomake.domain.elements.custom.{PreferenceRow, PreferenceTable, SocialSecurityNumber}
import fi.vm.sade.haku.oppija.lomake.domain.elements.questions.{CheckBox, DateQuestion, OptionQuestion, TextArea, TextQuestion}
import fi.vm.sade.haku.oppija.lomake.domain.elements.{HiddenValue, Phase, Text, Theme, TitledGroup}
import fi.vm.sade.haku.oppija.lomake.domain.rules.{AddElementRule, RelatedQuestionRule}
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.HakemusConverter.FlatAnswers
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioService
import fi.vm.sade.omatsivut.localization.Translations
import scalatags.Text.TypedTag
import fi.vm.sade.omatsivut.koulutusinformaatio.Koulutus
import org.joda.time.DateTime
import fi.vm.sade.haku.oppija.hakemus.domain.util.ApplicationUtil
import fi.vm.sade.omatsivut.koulutusinformaatio.Opetuspiste
import org.joda.time.format.DateTimeFormat
import fi.vm.sade.omatsivut.koulutusinformaatio.Opetuspiste
import fi.vm.sade.haku.oppija.lomake.domain.elements.Link
import fi.vm.sade.omatsivut.koulutusinformaatio.Address
import fi.vm.sade.omatsivut.koulutusinformaatio.Liitepyynto

case class HakemusPreviewGenerator(implicit val appConfig: AppConfig, val language: Language.Language) extends Logging {
  import scala.collection.JavaConversions._
  import scalatags.Text.all._
  private val applicationDao = appConfig.springContext.applicationDAO
  private val applicationSystemService = appConfig.springContext.applicationSystemService
  val koulutusInformaatio = KoulutusInformaatioService.apply
  val dateFormat = DateTimeFormat.forPattern("dd.M.yyyy")

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
    val addInfos = for(addInfo <- applicationSystem.getAdditionalInformationElements()) yield ElementWrapper.wrapFiltered(addInfo, answers)

    def questionsPreview(element: ElementWrapper): List[TypedTag[String]] = {
      element.element match {
        case _: GradeGrid => List(gradeGridPreview(element))
        case _: PreferenceTable => List(preferenceTablePreview(element))
        case _: TextArea => List(textQuestionPreview(element))
        case _: SocialSecurityNumber => List(textQuestionPreview(element))
        case _: TextQuestion => List(textQuestionPreview(element))
        case _: OptionQuestion => List(optionQuestionPreview(element))
        case _: CheckBox => List(checkBoxPreview(element))
        case _: Theme => themePreview(element)
        case _: Phase => childrenPreview(element)
        case _: RelatedQuestionRule => childrenPreview(element)
        case _: Text => List(textPreview(element))
        case link: Link => List(linkPreview(element, link))
        case _: TitledGroup => List(titledGroupPreview(element))
        case _: DateQuestion => List(textPreview(element))
        case _: AddElementRule => Nil
        case _: HiddenValue => Nil // info about attachments are added separately in the end of the document
        case _ =>
          logger.warn("Ignoring element " + element.element.getType + ": " + element.id)
          Nil
      }
    }

    def additionalInformationElementsPreview(): List[TypedTag[String]] = {
      (for(addInfo <- addInfos) yield additionalInformationElementPreview(addInfo)
      ).flatten.toList
    }

    def additionalInformationElementPreview(element: ElementWrapper): List[TypedTag[String]] = {
      if(element.children.isEmpty) {
        Nil
      }
      else {
        List(div(`class` := "theme")(
            hr(),
            questionsPreview(element)
        ))
      }
    }

    def discretionaryAttachmentsInfoPreview(): List[TypedTag[String]] = {
      val aoInfo = ApplicationUtil.getDiscretionaryAttachmentAOIds(application)
      if (aoInfo.isEmpty) {
        Nil
      }
      else {
        List(div(`class` := "theme")(
          h2(Translations.getTranslation("applicationPreview", "discretionary")),
          p(Translations.getTranslation("applicationPreview", "discretionary_info")),
          for (info <- aoInfo.map(koulutusInformaatio.liitepyynto(_))) yield attachmentInfoPreview(info)
        ))
      }
    }

    def attachmentsAnchor(): List[TypedTag[String]] = {
      List(a(name := "liitteet"))
    }

    def attachmentsInfoPreview(): List[TypedTag[String]] = {
      val aoInfos = ApplicationUtil.getHigherEdAttachmentAOIds(application).mapValues(_.filterNot(_.isEmpty()).map(koulutusInformaatio.liitepyynto(_)))
      if (aoInfos.isEmpty) {
        Nil
      }
      else {
          List(div(`class` := "theme")(
            h2(Translations.getTranslation("applicationPreview", "attachments")),
            for (baseEducation <- aoInfos.keySet.toList) yield div(
                p(Translations.getTranslation("applicationPreview", "attachments_info_" + baseEducation)),
                for (info <- aoInfos(baseEducation)) yield attachmentInfoPreview(info)
            )
          ))
      }
    }

    def attachmentInfoPreview(info: Liitepyynto): List[TypedTag[String]] = {
      if(info.address.isDefined) {
        List(div(`class` := "group")(
          elemIfNotEmptyString(h3(_), info.name),
          attachmentAddressInfoPreview(info.address.get, info.deadline)
        ))
      }
      else {
        Nil
      }
    }

    def attachmentAddressInfoPreview(address: Address, deliveryDeadline: Option[Long]): List[TypedTag[String]] = {
      List(
        elemIfNotEmptyString(div(_), address.streetAddress),
        elemIfNotEmptyString(div(_), address.streetAddress2),
        elemIfNotEmptyString(div(_), address.postalCode),
        elemIfNotEmptyString(div(_), address.postOffice ),
        elemIfNotEmpty[Long](div(_), deliveryDeadline, formatDeadlinePreview)
      ).flatten
    }

    def formatDeadlinePreview(date: Long): String = {
      Translations.getTranslation("applicationPreview", "attachments_deadline") + " " + (new DateTime(date)).toString(dateFormat)
    }

    def elemIfNotEmptyString(elem: String => TypedTag[String], value: Option[String]): Option[TypedTag[String]] = {
      elemIfNotEmpty[String](elem, value, _.toString())
    }

    def elemIfNotEmpty[T](elem: String => TypedTag[String], value: Option[T], format: T => String): Option[TypedTag[String]] = {
      if(value.isDefined && format(value.get).trim().length() > 0 ) {
        Some(elem(format(value.get).trim()))
      }
      else {
        None
      }
    }

    def themePreview(element: ElementWrapper): List[TypedTag[String]] = {
      if(element.children.isEmpty) {
        Nil
      }
      else {
        List(div(`class` := "theme")(h2(element.title), childrenPreview(element)))
      }
    }

    def childrenPreview(element: ElementWrapper) = {
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
        case true => Translations.getTranslation("message", "yes")
        case false => Translations.getTranslation("message", "no")
      }
      questionPreview(element.title, translatedAnswer)
    }

    def titledGroupPreview(element: ElementWrapper) = {
      div(`class` := "group")(
        h3(element.title) :: element.children.flatMap(questionsPreview)
      )
    }

    def preferenceTablePreview(tableElement: ElementWrapper) = {
      ul(`class` := "preference-table")(
        tableElement.children.zipWithIndex.flatMap { case (childElement, index) =>
          childElement.element match {
            case row: PreferenceRow =>
              answers(row.getEducationOidInputId) match {
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
                  ) ++ preferenceQuestionsPreview(childElement)
                ))
              }
            case _ =>
              logger.warn("Ignoring preference table element " + childElement.element.getType + ": " + childElement.id)
              Nil
          }
        }
      )
    }

    def preferenceQuestionsPreview(element: ElementWrapper) = {
      childrenPreview(element).toList match {
        case Nil =>
          Nil
        case children =>
          List(div(`class` := "questions")(h3(Translations.getTranslation("applicationPreview", "preferences_questions")), children))
      }
    }

    def attrs(el: ElementWrapper): List[Modifier] = {
      el.element.getAttributes.toMap.map { case (key, value) =>
        key.attr := value
      }.toList
    }

    def gradeGridPreview(gridElement: ElementWrapper) = {
      table(`class` := "gradegrid")(
        thead(
          td("colspan".attr := 2)(Translations.getTranslation("applicationPreview", "subject")) :: (if (gridElement.element.asInstanceOf[GradeGrid].isExtraColumn) {
            List(td(Translations.getTranslation("applicationPreview", "mutual_subject")), td(Translations.getTranslation("applicationPreview", "optional_subject")), td(Translations.getTranslation("applicationPreview", "second_optional_subject")))
          } else {
            List(td(Translations.getTranslation("applicationPreview", "grade")))
          })
        ),
        tbody(gridElement.children.map { row =>
          tr(row.children.map { column =>
            td(attrs(column))( // <- practically there's a colspan attribute that needs to be set
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

    def linkPreview(element: ElementWrapper, link: Link) = {
      a(`href` := ElementWrapper.t(link.getUrl()), `target` := "_blank")(element.title)
    }

    def formatOid(oid: String): String = {
      val split = oid.split("\\.").toList
      split match {
        case Nil => ""
        case parts => parts.last
      }
    }

    def formatDate(date: Date) = {
      new SimpleDateFormat("d.M.yyyy HH:mm").format(date)
    }

    html(
      head(link(href := "/omatsivut/css/preview.css", rel:= "stylesheet", `type` := "text/css")),
      body(
        header(
          h1(ElementWrapper.t(applicationSystem.getName)),
          h2(answers.get("Etunimet").getOrElse("") + " " + answers.get("Sukunimi").getOrElse("")),
          div(`class` := "detail application-received")(label(Translations.getTranslation("applicationPreview", "received")), span(formatDate(application.getReceived))),
          div(`class` := "detail application-id")(label(Translations.getTranslation("applicationPreview", "applicationId")), span(formatOid(application.getOid)))
        )
        ::
        form.getElementsOfType[Phase].flatMap(questionsPreview)
        :::
        additionalInformationElementsPreview()
        :::
        attachmentsAnchor()
        :::
        discretionaryAttachmentsInfoPreview()
        :::
        attachmentsInfoPreview()
      )
    ).toString
  }
}
