package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.HakemusRepositoryComponent
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.{Answers, HakutoiveData}
import fi.vm.sade.hakemuseditori.hakemus.domain.{Hakemus, HakemusLike, HakemusMuutos}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.{AnswerId, QuestionNode}
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.{sanitizeHakemus, Oid}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import org.json4s.jackson.Serialization
import org.scalatra.json.JacksonJsonSupport
import org.scalatra._

import scala.util.{Failure, Success, Try}

case class InsecureResponse(jsonWebToken: String, response: Any)

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    OppijanTunnistusComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with HakemusEditori with HakemusEditoriUserContext {

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi muokata hakemusta heikosti tunnistautuneena"

    def getPersonOidFromSession: String = {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) => hakemusJWT.personOid
        case _ => throw new RuntimeException("Invalid Json Web Token")
      }
    }

    def user = Oppija(getPersonOidFromSession)
    private val hakemusEditori = newEditor(this)

    val jwt = new JsonWebToken(appConfig.settings.hmacKey)

    def getHakemusInfoFromBearerToken: Try[HakemusJWT] = {
      val bearerMatch = """Bearer (.+)""".r
      request.getHeader("Authorization") match {
        case bearerMatch(token) => jwt.decode(token)
        case _ => Failure(new RuntimeException("Invalid authorization header"))
      }
    }

    before() {
      contentType = formats("json")
    }

    put("/:oid") {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) =>
          val updated = Serialization.read[HakemusMuutos](request.body)
          hakemusEditori.updateHakemus(updated) match {
            case Success(hakemus) =>
              val answersFromThisSession = NonSensitiveHakemusInfo.answerIds(updated.answers) & NonSensitiveHakemusInfo.answerIds(hakemus.answers)
              Ok(InsecureResponse(jwt.encode(HakemusJWT(hakemusJWT.oid, answersFromThisSession, hakemusJWT.personOid)),
                sanitizeHakemus(hakemus, answersFromThisSession)))
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
          InsecureResponse(jwt.encode(hakemusJWT),
            NonSensitiveHakemusInfo.sanitize(hakemusRepository.getHakemus(hakemusJWT.oid).get, hakemusJWT.answersFromThisSession).hakemusInfo)
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
          InsecureResponse(jwt.encode(HakemusJWT(hakemusOid, NonSensitiveHakemusInfo.nonSensitiveAnswers, personOid)),
            NonSensitiveHakemusInfo.sanitize(hakemusRepository.getHakemus(hakemusOid).get, NonSensitiveHakemusInfo.nonSensitiveAnswers).hakemusInfo)
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
          val muutos = Serialization.read[HakemusMuutos](request.body)
          hakemusEditori.validateHakemus(muutos) match {
            case Some(hakemusInfo) =>
              InsecureResponse(jwt.encode(hakemusJWT),
                NonSensitiveHakemusInfo.sanitize(hakemusInfo, NonSensitiveHakemusInfo.answerIds(muutos.answers)).hakemusInfo
              )
            case _ =>
              InternalServerError("error" -> "Internal service unavailable")
          }
        case Failure(e) =>
          InternalServerError("error" -> e.getMessage)
      }
    }

    def definedHakukohdes(hakemus: HakemusLike): List[HakutoiveData] = {
      hakemus.preferences.filter(_.contains(PREFERENCE_FRAGMENT_OPTION_ID))
    }

    def newHakukohteet(initialHakukohdeOids: List[Oid], muutos: HakemusMuutos): List[HakutoiveData] = {
      val isNotExistingHakukohde = (hakukohde: HakutoiveData) => !initialHakukohdeOids.contains(hakukohde(PREFERENCE_FRAGMENT_OPTION_ID))
      definedHakukohdes(muutos).filter(isNotExistingHakukohde)
    }

    def newQuestions(initialHakukohdeOids: List[Oid], muutos: HakemusMuutos): List[QuestionNode] = {
      hakemusEditori.validateHakemus(muutos.copy(hakutoiveet = newHakukohteet(initialHakukohdeOids, muutos)))
        .map(_.questions)
        .getOrElse(List.empty)
    }
  }

}
