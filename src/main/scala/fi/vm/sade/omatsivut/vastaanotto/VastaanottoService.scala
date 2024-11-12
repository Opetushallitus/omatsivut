package fi.vm.sade.omatsivut.vastaanotto

import java.text.SimpleDateFormat
import java.util.Calendar
import javax.servlet.http.HttpServletRequest

import fi.vm.sade.groupemailer.{EmailData, EmailMessage, EmailRecipient, GroupEmailComponent}
import fi.vm.sade.hakemuseditori.HakemusEditoriComponent
import fi.vm.sade.hakemuseditori.auditlog.{Audit, SaveVastaanotto}
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, HakemusRepositoryComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.valintatulokset.domain.{VastaanotaSitovasti, VastaanottoAction}
import org.scalatra.{ActionResult, Forbidden, InternalServerError, Ok}

import scala.util.{Failure, Success, Try}

trait VastaanottoComponent {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    GroupEmailComponent =>

  def vastaanottoService(implicit language: Language): VastaanottoService

  class VastaanottoService()(implicit language: Language) extends JsonFormats {

    def vastaanota(request: HttpServletRequest, hakemusOid: String, hakukohdeOid: String, henkiloOid: String, vastaanotto: Vastaanotto, hakemus: HakemusInfo): ActionResult = {
      val hakuOid: String = hakemus.hakemus.haku.map(_.oid).getOrElse("")
      val email: Option[String] = hakemus.hakemus.email
      val henkiloOidHakemukselta = hakemus.hakemus.personOid
      if (!henkiloOid.equals(henkiloOidHakemukselta)) {
        logger.info(s"Tallennetaan vastaanotto hakemukselle $hakemusOid, henkilön session mukana kulkeva oid ($henkiloOid) " +
          s"on eri kuin hakemuksella oleva oid ($henkiloOidHakemukselta). Tehdään vastaanoton tallennus hakemuksen oidille.")
      }

      Try(valintatulosService.vastaanota(henkiloOidHakemukselta, hakemusOid, hakukohdeOid, vastaanotto.vastaanottoAction)) match {
        case Success(result) => {
          Audit.oppija.log(SaveVastaanotto(request, henkiloOidHakemukselta, hakemusOid, hakukohdeOid, hakuOid, vastaanotto.vastaanottoAction))
          if (result) {
            email match {
              case None => logger.error(s"""Vastaanottosähköpostia ei voitu lähettää, koska sähköpostiosoitetta ei löytynyt. HakemusOid: $hakemusOid""")
              case Some(email: String) => {
                vastaanottoService.sendVastaanottoEmail(hakuOid, vastaanotto, email)
              }
            }
            Ok(hakemus)
          } else {
            Forbidden("error" -> "Not receivable")
          }
        }
        case Failure(t) => {
          logger.error("failure in background service call", t)
          InternalServerError("error" -> "Background service failed")
        }
      }
    }

    private def sendVastaanottoEmail(hakuOid: String, vastaanotto: Vastaanotto, email: String): Unit = {
      try {
        val haku = tarjontaService.haku(hakuOid, language).get
        val subject = translations.getTranslation("message", "acceptEducation", "email", "subject")

        val dateFormat = new SimpleDateFormat(translations.getTranslation("message", "acceptEducation", "email", "dateFormat"))
        val dateAndTime = dateFormat.format(Calendar.getInstance().getTime)
        val vastaanottoActionString =
          if (haku.toisenasteenhaku && VastaanotaSitovasti.equals(vastaanotto.vastaanottoAction)) {
            vastaanotto.vastaanottoAction.toString + "ToinenAste"
          } else {
            vastaanotto.vastaanottoAction.toString
          }
        val answer = translations.getTranslation("message", "acceptEducation", "email", "tila", vastaanottoActionString)
        val aoInfoRow = List(answer, vastaanotto.tarjoajaNimi, vastaanotto.hakukohdeNimi).mkString(" - ")
        val body = translations.getTranslation("message", "acceptEducation", "email", "body")
          .format(dateAndTime, aoInfoRow)
          .replace("\n", "\n<br>")

        val emailMessage = EmailMessage("omatsivut", subject, body, html = true)
        val recipients = List(EmailRecipient(email))
        groupEmailService.sendMailWithoutTemplate(EmailData(emailMessage, recipients))
      } catch {
        case e: Exception => logger.error(
          s"""Vastaanottosähköpostin lähetys epäonnistui: hakuOid / hakukohdeNimi / tarjoajaNimi / email :
            $hakuOid / ${vastaanotto.hakukohdeNimi} / ${vastaanotto.tarjoajaNimi} / $email""".stripMargin)
      }
    }
  }
}

case class Vastaanotto(vastaanottoAction: VastaanottoAction, hakukohdeNimi: String = "", tarjoajaNimi: String = "")
