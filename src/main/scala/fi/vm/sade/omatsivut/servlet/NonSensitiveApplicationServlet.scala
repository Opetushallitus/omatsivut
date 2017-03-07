package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{ImmutableLegacyApplicationWrapper, HakemusInfo, HakemusRepositoryComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.answerIds
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{ExpiredTokenException, InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import fi.vm.sade.omatsivut.{NonSensitiveHakemus, NonSensitiveHakemusInfo, NonSensitiveHakemusInfoSerializer, NonSensitiveHakemusSerializer}
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport

import scala.util.{Failure, Success, Try}

sealed trait InsecureResponse {
  def jsonWebToken: String
}

case class InsecureHakemus(jsonWebToken: String, response: NonSensitiveHakemus) extends InsecureResponse
case class InsecureHakemusInfo(jsonWebToken: String, response: NonSensitiveHakemusInfo) extends InsecureResponse

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    OppijanTunnistusComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with HakemusEditori with HakemusEditoriUserContext {
    implicit val jsonFormats = JsonFormats.jsonFormats ++ List(new NonSensitiveHakemusSerializer, new NonSensitiveHakemusInfoSerializer)
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

    private def fetchHakemus(oid: String): Try[HakemusInfo] = {
      def fetchTulosForNonHetuHakemus(hakemus: ImmutableLegacyApplicationWrapper) = {
        hakemus.henkilotunnus.isEmpty
      }
      hakemusRepository.getHakemus(oid, fetchTulosForHakemus = fetchTulosForNonHetuHakemus)
        .fold[Try[HakemusInfo]](Failure(new NoSuchElementException(s"Hakemus $oid not found")))(Success(_))
    }

    before() {
      contentType = formats("json")
    }

    get("/tuloskirje/:hakuOid") {
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

    put("/:oid") {
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

    get("/application/session") {
      (for {
        token <- jwtAuthorize
        hakemus <- fetchHakemus(token.oid)
      } yield {
        Ok(InsecureHakemusInfo(jwt.encode(token), new NonSensitiveHakemusInfo(hakemus, token.answersFromThisSession)))
      }).get
    }

    get("/application/token/:token") {
      (for {
        hakemusOid <- oppijanTunnistusService.validateToken(params("token"))
        personOid <- applicationRepository.findStoredApplicationByOid(hakemusOid)
          .fold[Try[String]](Failure(new NoSuchElementException(s"Hakemus $hakemusOid not found")))(h => Success(h.personOid))
        hakemus <- fetchHakemus(hakemusOid)
      } yield {
        Ok(InsecureHakemusInfo(jwt.encode(HakemusJWT(hakemusOid, Set(), personOid)),
          new NonSensitiveHakemusInfo(hakemus, Set())))
      }).get
    }

    post("/validate/:oid") {
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
