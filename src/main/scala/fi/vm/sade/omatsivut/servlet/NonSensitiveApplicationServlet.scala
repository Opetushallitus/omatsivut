package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, HakemusRepositoryComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.answerIds
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{InvalidTokenException, OppijanTunnistusComponent}
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

    def user = Oppija(getPersonOidFromSession)

    private def getPersonOidFromSession: String = {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) => hakemusJWT.personOid
        case _ => throw new RuntimeException("Invalid Json Web Token")
      }
    }

    private def getHakemusInfoFromBearerToken: Try[HakemusJWT] = {
      val bearerMatch = """Bearer (.+)""".r
      request.getHeader("Authorization") match {
        case bearerMatch(token) => jwt.decode(token)
        case _ => Failure(new RuntimeException("Invalid authorization header"))
      }
    }

    private def newAnswersFromTheSession(update: HakemusMuutos, persistedHakemus: HakemusInfo, hakemusJWT: HakemusJWT): Set[AnswerId] = {
      val nonPersistedAnswers = answerIds(update.answers) &~ answerIds(persistedHakemus.hakemus.answers)
      hakemusJWT.answersFromThisSession ++ nonPersistedAnswers
    }

    before() {
      contentType = formats("json")
    }

    put("/:oid") {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) =>
          val update = Serialization.read[HakemusMuutos](request.body)
          val newAnswers = newAnswersFromTheSession(update, hakemusRepository.getHakemus(hakemusJWT.oid).get, hakemusJWT)
          hakemusEditori.updateHakemus(update) match {
            case Success(hakemus) =>
              Ok(InsecureHakemus(jwt.encode(HakemusJWT(hakemusJWT.oid, newAnswers, hakemusJWT.personOid)),
                new NonSensitiveHakemus(hakemus, newAnswers)))
            case Failure(e: ForbiddenException) =>
              Forbidden("errors" -> "Forbidden")
            case Failure(e: ValidationException) =>
              BadRequest(e.validationErrors)
            case Failure(e: Throwable) =>
              InternalServerError("error" -> "Internal service unavailable")
          }
        case Failure(e) =>
          InternalServerError("error" -> e.getMessage)
      }
    }

    get("/application/session") {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) =>
          InsecureHakemusInfo(jwt.encode(hakemusJWT),
            new NonSensitiveHakemusInfo(hakemusRepository.getHakemus(hakemusJWT.oid).get, hakemusJWT.answersFromThisSession))
        case Failure(e) => InternalServerError("error" -> e.getMessage)
      }
    }

    get("/application/token/:token") {
      oppijanTunnistusService.validateToken(params("token")) match {
        case Success(hakemusOid) =>
          val personOid = applicationRepository
            .findStoredApplicationByOid(hakemusOid)
            .getOrElse(throw new RuntimeException("Application not found: " + hakemusOid))
            .personOid
          InsecureHakemusInfo(jwt.encode(HakemusJWT(hakemusOid, Set(), personOid)),
            new NonSensitiveHakemusInfo(hakemusRepository.getHakemus(hakemusOid).get, Set()))
        case Failure(e: InvalidTokenException) =>
          NotFound("errorType" -> "invalidToken")
        case Failure(exception) =>
          logger.error("Failed to validate token", exception)
          InternalServerError("error" -> "Failed to validate token")
      }
    }

    post("/validate/:oid") {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) =>
          val update = Serialization.read[HakemusMuutos](request.body)
          val hakemusBeforeValidation = hakemusRepository.getHakemus(hakemusJWT.oid).get
          hakemusEditori.validateHakemus(update) match {
            case Some(hakemus) =>
              InsecureHakemusInfo(jwt.encode(hakemusJWT),
                new NonSensitiveHakemusInfo(hakemus, newAnswersFromTheSession(update, hakemusBeforeValidation, hakemusJWT)))
            case _ =>
              InternalServerError("error" -> "Internal service unavailable")
          }
        case Failure(e) =>
          InternalServerError("error" -> e.getMessage)
      }
    }
  }

}
