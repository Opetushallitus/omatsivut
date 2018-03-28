package fi.vm.sade.omatsivut.servlet

import java.text.SimpleDateFormat
import java.util.Calendar

import fi.vm.sade.groupemailer.{EmailData, EmailMessage, EmailRecipient, GroupEmailComponent}
import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.auditlog.{Audit, SaveVastaanotto}
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{FetchIfNoHetuOrToinenAste, HakemusInfo, HakemusRepositoryComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.domain.{Ilmoittautuminen, VastaanotaSitovasti}
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.answerIds
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{ExpiredTokenException, InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import fi.vm.sade.omatsivut.{NonSensitiveHakemus, NonSensitiveHakemusInfo, NonSensitiveHakemusInfoSerializer, NonSensitiveHakemusSerializer}
import org.json4s._
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport

import scala.util.{Failure, Success, Try}

sealed trait InsecureResponse {
  def jsonWebToken: String
}

case class InsecureHakemus(jsonWebToken: String, response: NonSensitiveHakemus) extends InsecureResponse
case class InsecureHakemusInfo(jsonWebToken: String, response: NonSensitiveHakemusInfo, oiliJwt: String = null) extends InsecureResponse

trait VastaanottoEmailContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    TarjontaComponent with
    GroupEmailComponent =>

  def vastaanota(hakemusOid: String, hakukohdeOid: String, hakuOid: String, henkiloOid: String, requestBody: String, emailOpt: Option[String],
                 fetchHakemus: () => Option[HakemusInfo])(implicit jsonFormats: Formats, language: Language): ActionResult = {
    def sendEmail(clientVastaanotto: ClientSideVastaanotto) = {
      emailOpt match {
        case Some(email: String) =>
          val haku = tarjontaService.haku(hakuOid, language).get
          val subject = translations.getTranslation("message", "acceptEducation", "email", "subject")

          val dateFormat = new SimpleDateFormat(translations.getTranslation("message", "acceptEducation", "email", "dateFormat"))
          val dateAndTime = dateFormat.format(Calendar.getInstance().getTime)
          val vastaanottoActionString =
            if (haku.toisenasteenhaku && VastaanotaSitovasti.equals(clientVastaanotto.vastaanottoAction)) {
              clientVastaanotto.vastaanottoAction.toString + "ToinenAste"
            } else {
              clientVastaanotto.vastaanottoAction.toString
            }
          val answer = translations.getTranslation("message", "acceptEducation", "email", "tila", vastaanottoActionString)
          val aoInfoRow = List(answer, clientVastaanotto.tarjoajaNimi, clientVastaanotto.hakukohdeNimi).mkString(" - ")
          val body = translations.getTranslation("message", "acceptEducation", "email", "body")
            .format(dateAndTime, aoInfoRow)
            .replace("\n", "\n<br>")

          val emailMessage = EmailMessage("omatsivut", subject, body, html = true)
          val recipients = List(EmailRecipient(email))
          groupEmailService.sendMailWithoutTemplate(EmailData(emailMessage, recipients))
        case _ =>

      }
    }
    val clientVastaanotto = Serialization.read[ClientSideVastaanotto](requestBody)
    try {
      if (valintatulosService.vastaanota(henkiloOid, hakemusOid, hakukohdeOid, clientVastaanotto.vastaanottoAction)) {
        try {
          sendEmail(clientVastaanotto)
        } catch {
          case e: Exception => logger.error(
            s"""Vastaanottosähköpostin lähetys epäonnistui: haku / hakukohde / hakemus / hakija / email / clientVastaanotto :
            $hakuOid / $hakukohdeOid / $hakemusOid / $henkiloOid / ${emailOpt} / $clientVastaanotto""".stripMargin)
        }
        Audit.oppija.log(SaveVastaanotto(henkiloOid, hakemusOid, hakukohdeOid, hakuOid, clientVastaanotto.vastaanottoAction))
        fetchHakemus() match {
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

}

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    GroupEmailComponent with
    VastaanottoEmailContainer with
    OppijanTunnistusComponent with
    TarjontaComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport with HakemusEditori with HakemusEditoriUserContext {
    override implicit val jsonFormats = JsonFormats.jsonFormats ++ List(new NonSensitiveHakemusSerializer, new NonSensitiveHakemusInfoSerializer)
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi muokata hakemusta heikosti tunnistautuneena"
    private val hakemusEditori = newEditor(this)
    private val jwt = new JsonWebToken(appConfig.settings.hmacKey)

    class UnauthorizedException(msg: String) extends RuntimeException(msg)
    class ForbiddenException(msg: String) extends RuntimeException(msg)

    error {
      case e: UnauthorizedException => Unauthorized("error" -> "Unauthorized")
      case e: ForbiddenException => Forbidden("error" -> "Forbidden")
      case e: InvalidTokenException => Forbidden("error" -> "Forbidden")
      case e: ExpiredTokenException => Forbidden("error" -> "expiredToken")
      case e: ValidationException => BadRequest(e.validationErrors)
      case e: NoSuchElementException =>
        logger.warn(request.getMethod + " " + requestPath, e)
        NotFound("error" -> "Not found")
      case e: Exception =>
        logger.error(request.getMethod + " " + requestPath, e)
        InternalServerError("error" -> "Internal server error")
    }

    def user = Oppija(getPersonOidFromSession)

    private def getPersonOidFromSession: String = {
      jwtAuthorize match {
        case Success(hakemusJWT) => hakemusJWT.personOid
        case Failure(e) => throw e
      }
    }

    private def jwtAuthorize: Try[HakemusJWT] = {
      val bearerMatch = """Bearer (.+)""".r
      request.getHeader("Authorization") match {
        case bearerMatch(jwtString) => jwt.decode(jwtString)
          .transform(Success(_), e => Failure(new ForbiddenException(e.getMessage)))
        case _ => Failure(new UnauthorizedException("Invalid Authorization header"))
      }
    }

    private def newAnswersFromTheSession(update: HakemusMuutos, hakemusJWT: HakemusJWT): Set[AnswerId] = {
      val nonPersistedAnswers = answerIds(update.answers)
      hakemusJWT.answersFromThisSession ++ nonPersistedAnswers
    }

    private def fetchHakemus(hakemusOid: String, personOid: Option[String]): Try[HakemusInfo] = {
      personOid.map(hakemusEditori.fetchByHakemusOid(_, hakemusOid, FetchIfNoHetuOrToinenAste))
        .getOrElse(hakemusRepository.getHakemus(hakemusOid, FetchIfNoHetuOrToinenAste))
        .fold[Try[HakemusInfo]](Failure(new NoSuchElementException(s"Hakemus $hakemusOid not found")))(h => Success(h.withoutKelaUrl))
    }

    before() {
      contentType = formats("json")
    }

    get("/applications/tuloskirje/:hakuOid") {
      val hakuOid = params("hakuOid")
      (for {
        token <- jwtAuthorize
        tuloskirje <- Try(fetchTuloskirje(token.personOid, hakuOid))
      } yield {
        tuloskirje match {
          case Some(data) => Ok(tuloskirje, Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
          case None => InternalServerError("error" -> "Internal Server Error")
        }
      }).get
    }

    post("/ilmoittaudu") {
      val body = parsedBody.extract[Ilmoittautuminen]
      body.muokkaaja = user().oid
      valintatulosService.ilmoittaudu(params("hakuOid"), params("hakemusOid"), body)
    }

    put("/applications/:oid") {
      (for {
        token <- jwtAuthorize
        update <- Try(Serialization.read[HakemusMuutos](request.body))
        newAnswers <- Success(newAnswersFromTheSession(update, token))
        updatedHakemus <- hakemusEditori.updateHakemus(NonSensitiveHakemusInfo.sanitizeHakemusMuutos(update, newAnswers))
      } yield {
        Ok(InsecureHakemus(jwt.encode(HakemusJWT(token.oid, newAnswers, token.personOid)),
          new NonSensitiveHakemus(updatedHakemus, newAnswers)))
      }).get
    }

    get("/applications/application/session") {
      (for {
        token <- jwtAuthorize
        hakemus <- fetchHakemus(token.oid, Some(token.personOid))
      } yield {
        Ok(InsecureHakemusInfo(
          jwt.encode(token),
          new NonSensitiveHakemusInfo(hakemus, token.answersFromThisSession),
          oiliJwt = jwt.createOiliJwt(token.personOid)
        ))
      }).get
    }

    post("/applications/vastaanota/:hakemusOid/hakukohde/:hakukohdeOid") {
      val hakemusOid = params("hakemusOid")
      val hakukohdeOid = params("hakukohdeOid")
      val henkiloOid = getPersonOidFromSession

      hakemusEditori.fetchByHakemusOid(henkiloOid, hakemusOid, FetchIfNoHetuOrToinenAste) match {
        case Some(hakemus) => vastaanota(
          hakemusOid,
          hakukohdeOid,
          hakemus.hakemus.haku.oid,
          henkiloOid,
          request.body,
          hakemus.hakemus.email,
          () => Some(hakemus)
        )
        case None => NotFound("error" -> "Not found")
      }
    }

    get("/applications/application/token/:token") {
      (for {
        metadata <- oppijanTunnistusService.validateToken(params("token"))
        hakemus <- fetchHakemus(metadata.hakemusOid, metadata.personOid)
      } yield {
        Ok(InsecureHakemusInfo(
          jwt.encode(HakemusJWT(metadata.hakemusOid, Set(), hakemus.hakemus.personOid)),
          new NonSensitiveHakemusInfo(hakemus, Set()),
          oiliJwt = jwt.createOiliJwt(hakemus.hakemus.personOid)
        ))
      }).get
    }

    post("/applications/validate/:oid") {
      (for {
        token <- jwtAuthorize
        update <- Try(Serialization.read[HakemusMuutos](request.body))
        validatedHakemus <- hakemusEditori.validateHakemus(update)
          .fold[Try[HakemusInfo]](Failure(new RuntimeException))(Success(_))
      } yield {
        Ok(InsecureHakemusInfo(jwt.encode(token),
          new NonSensitiveHakemusInfo(validatedHakemus, newAnswersFromTheSession(update, token))))
      }).get
    }
  }

}
