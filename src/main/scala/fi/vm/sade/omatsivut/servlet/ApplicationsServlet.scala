package fi.vm.sade.omatsivut.servlet

import java.text.SimpleDateFormat
import java.util.Calendar

import fi.vm.sade.groupemailer.{EmailData, EmailMessage, EmailRecipient, GroupEmailComponent}
import fi.vm.sade.hakemuseditori.auditlog.{AuditLoggerComponent, SaveVastaanotto}
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{ApplicationValidatorComponent, HakemusRepositoryComponent, SpringContextComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Vastaanotto
import fi.vm.sade.hakemuseditori.{HakemusEditoriComponent, HakemusEditoriUserContext, UpdateResult}
import fi.vm.sade.omatsivut.NonSensitiveHakemus
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.oppijantunnistus.{InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import fi.vm.sade.omatsivut.valintarekisteri.ValintaRekisteriComponent
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._

import scala.util.{Failure, Success}

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with LomakeRepositoryComponent with
    HakemusRepositoryComponent with
    ValintatulosServiceComponent with
    ValintaRekisteriComponent with
    ApplicationValidatorComponent with
    HakemusPreviewGeneratorComponent with
    SpringContextComponent with
    AuditLoggerComponent with
    GroupEmailComponent with
    TranslationsComponent with
    OppijanTunnistusComponent =>

  class ApplicationsServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with AuthenticationRequiringServlet with HakemusEditoriUserContext {
    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    get("/") {
      hakemusEditori.fetchByPersonOid(personOid())
    }

    get("/application/:token") {
      oppijanTunnistusService.validateToken(params("token")) match {
        case Success(hakemusOid) =>
          hakemusEditori.fetchByHakemusOid(hakemusOid) match {
            case Some(hakemusInfo) =>
              val hakemus = hakemusInfo.hakemus
              NonSensitiveHakemus(hakemus.oid, hakemus.hakutoiveet)
            case _ =>
              logger.error("Token was valid but hakemus not found! Token: " + params("token") + ", hakemusOid: " + hakemusOid)
              NotFound("error" -> "Hakemus not found")
          }
        case Failure(e: InvalidTokenException) =>
          "tokenValid" -> false
        case Failure(exception) =>
          logger.error("Failed to validate token", exception)
          InternalServerError("error" -> "Failed to validate token")
      }
    }

    put("/:oid") {
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      val response: Option[ActionResult] = hakemusEditori.updateHakemus(updated)
        .map { case UpdateResult(status, body) => ActionResult(ResponseStatus(status), body, Map.empty)}
      response.getOrElse(InternalServerError("error" -> "Internal service unavailable"))
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

    post("/vastaanota/:hakuOid/:hakemusOid") {
      val hakemusOid = params("hakemusOid")
      val hakuOid = params("hakuOid")
      val henkilo = personOid()
      if (!applicationRepository.exists(henkilo, hakemusOid)) {
        NotFound("error" -> "Not found")
      } else {
        val clientVastaanotto = Serialization.read[ClientSideVastaanotto](request.body)
        val vastaanotto = Vastaanotto(clientVastaanotto.hakukohdeOid, clientVastaanotto.tila, henkilo, "Muokkaus Omat Sivut -palvelussa")
        try{
          val ret = if(valintaRekisteriService.isEnabled) {
            valintaRekisteriService.vastaanota(henkilo, vastaanotto.hakukohdeOid, henkilo)
          } else {
            valintatulosService.vastaanota(hakemusOid, hakuOid, vastaanotto)
          }
          if(ret) {
            // Send a confirmation e-mail
            if (!clientVastaanotto.email.equals("")) {
              val subject = translations.getTranslation("message", "acceptEducation", "email", "subject")

              val dateFormat = new SimpleDateFormat(translations.getTranslation("message", "acceptEducation", "email", "dateFormat"))
              val dateAndTime = dateFormat.format(Calendar.getInstance().getTime())
              val answer = translations.getTranslation("message", "acceptEducation", "email", "tila", clientVastaanotto.tila)
              val aoInfoRow = List(answer, clientVastaanotto.tarjoajaNimi, clientVastaanotto.hakukohdeNimi).mkString(" - ")
              val body = translations.getTranslation("message", "acceptEducation", "email", "body")
                            .format(dateAndTime, aoInfoRow)
                            .replace("\n", "\n<br>")

              val email = EmailMessage("omatsivut", subject, body, html = true)
              val recipients = List(EmailRecipient(clientVastaanotto.email))
              groupEmailService.sendMailWithoutTemplate(EmailData(email, recipients))
            }
            auditLogger.log(SaveVastaanotto(personOid(), hakemusOid, hakuOid, vastaanotto))
            hakemusRepository.getHakemus(hakemusOid) match {
              case Some(hakemus) => hakemus
              case _ => NotFound("error" -> "Not found")
            }
          }
          else {
            Forbidden("error" -> "Not receivable")
          }

        } catch {
          case e =>
            logger.error("failure in background service call", e)
            InternalServerError("error" -> "Background service failed")

        }

      }
    }
  }
}

case class ClientSideVastaanotto(hakukohdeOid: String, tila: String, email: String = "",
                                 hakukohdeNimi: String = "", tarjoajaNimi: String = "")

