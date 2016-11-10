package fi.vm.sade.omatsivut.servlet

import java.text.SimpleDateFormat
import java.util.Calendar

import fi.vm.sade.groupemailer.{EmailData, EmailMessage, EmailRecipient, GroupEmailComponent}
import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.auditlog.{AuditLoggerComponent, SaveVastaanotto}
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{ApplicationValidatorComponent, HakemusRepositoryComponent, SpringContextComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.valintatulokset.domain._
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._

import scala.util.{Failure, Success}

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with LomakeRepositoryComponent with
    HakemusRepositoryComponent with
    ValintatulosServiceComponent with
    ApplicationValidatorComponent with
    HakemusPreviewGeneratorComponent with
    SpringContextComponent with
    AuditLoggerComponent with
    GroupEmailComponent with
    TranslationsComponent =>

  class ApplicationsServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with AuthenticationRequiringServlet with HakemusEditoriUserContext {
    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    get("/tuloskirje/:hakuOid") {
      val hakuOid = params("hakuOid")
      hakemusEditori.fetchTuloskirje(personOid(), hakuOid) match {
        case Some(tuloskirje) => Ok(tuloskirje, Map(
          "Content-Type" -> "application/octet-stream",
          "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
        case None => NotFound("error" -> "Not found")
      }
    }

    get("/") {
      hakemusEditori.fetchByPersonOid(personOid())
    }

    put("/:oid") {
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      hakemusEditori.updateHakemus(updated) match {
        case Success(body) => ActionResult(ResponseStatus(200), body, Map.empty)
        case Failure(e: ForbiddenException) => ActionResult(ResponseStatus(403), "error" -> "Forbidden", Map.empty)
        case Failure(e: ValidationException) => ActionResult(ResponseStatus(400), e.validationErrors, Map.empty)
        case Failure(e) => InternalServerError("error" -> "Internal service unavailable")
      }
    }


    post("/validate/:oid") {
      val muutos = Serialization.read[HakemusMuutos](request.body)

      hakemusEditori.validateHakemus(muutos) match {
        case Some(hakemusInfo) => hakemusInfo
        case _ => InternalServerError("error" -> "Internal service unavailable")
      }
    }

    get("/preview/:oid") {
      newHakemusPreviewGenerator(language).generatePreview(personOid(), params("oid")) match {
        case Some(previewHtml) =>
          contentType = formats("html")
          Ok(previewHtml)
        case None =>
          NotFound("error" -> "Not found")
      }
    }

    post("/vastaanota/:hakemusOid/hakukohde/:hakukohdeOid") {
      val hakemusOid = params("hakemusOid")
      val hakukohdeOid = params("hakukohdeOid")
      val henkiloOid = personOid()

      applicationRepository.findStoredApplicationByPersonAndOid(henkiloOid, hakemusOid) match {

        case Some(hakemus) if tarjontaService.haku(hakemus.hakuOid, Language.fi).exists(_.published) =>
          vastaanota(hakemusOid, hakukohdeOid, hakemus.hakuOid, henkiloOid, request.body)

        case _ => NotFound("error" -> "Not found")

      }
    }

    private def vastaanota(hakemusOid: String, hakukohdeOid: String, hakuOid: String, henkiloOid: String, requestBody: String): ActionResult = {
      val clientVastaanotto = Serialization.read[ClientSideVastaanotto](requestBody)
      try {
        if (valintatulosService.vastaanota(henkiloOid, hakemusOid, hakukohdeOid, clientVastaanotto.vastaanottoAction)) {
          try {
            sendEmail(clientVastaanotto)
          } catch {
            case e: Exception => logger.error(s"""Vastaanottosähköpostin lähetys epäonnistui: haku / hakukohde / hakemus / hakija / email / clientVastaanotto :
              $hakuOid / $hakukohdeOid / $hakemusOid / $henkiloOid / ${clientVastaanotto.email} / $clientVastaanotto""".stripMargin)
          }
          auditLogger.log(SaveVastaanotto(henkiloOid, hakemusOid, hakukohdeOid, hakuOid, clientVastaanotto.vastaanottoAction))
          hakemusRepository.getHakemus(hakemusOid) match {
            case Some(hakemus) => Ok(hakemus)
            case _ => NotFound("error" -> "Not found")
          }
        }
        else {
          Forbidden("error" -> "Not receivable")
        }

      } catch {
        case e: Throwable =>
          logger.error("failure in background service call", e)
          InternalServerError("error" -> "Background service failed")
      }
    }

    private def sendEmail(clientVastaanotto: ClientSideVastaanotto) = {
      if ("" != clientVastaanotto.email) {
        val subject = translations.getTranslation("message", "acceptEducation", "email", "subject")

        val dateFormat = new SimpleDateFormat(translations.getTranslation("message", "acceptEducation", "email", "dateFormat"))
        val dateAndTime = dateFormat.format(Calendar.getInstance().getTime)
        val answer = translations.getTranslation("message", "acceptEducation", "email", "tila", clientVastaanotto.vastaanottoAction.toString)
        val aoInfoRow = List(answer, clientVastaanotto.tarjoajaNimi, clientVastaanotto.hakukohdeNimi).mkString(" - ")
        val body = translations.getTranslation("message", "acceptEducation", "email", "body")
          .format(dateAndTime, aoInfoRow)
          .replace("\n", "\n<br>")

        val email = EmailMessage("omatsivut", subject, body, html = true)
        val recipients = List(EmailRecipient(clientVastaanotto.email))
        groupEmailService.sendMailWithoutTemplate(EmailData(email, recipients))
      }
    }

  }
}

case class ClientSideVastaanotto(vastaanottoAction: VastaanottoAction, email: String = "",
                                 hakukohdeNimi: String = "", tarjoajaNimi: String = "")

