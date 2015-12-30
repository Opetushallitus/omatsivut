package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.hakemus.HakemusRepositoryComponent
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.HakutoiveData
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.QuestionNode
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori._
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants._
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.Oid
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import org.json4s.jackson.Serialization
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{ActionResult, InternalServerError, NotFound, ResponseStatus}

import scala.util.{Failure, Success, Try}

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    OppijanTunnistusComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with HakemusEditori with HakemusEditoriUserContext {

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi muokata hakemusta heikosti tunnistautuneena"

    def getPersonOidFromSession: String = {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) =>
          applicationRepository
            .findStoredApplicationByOid(hakemusJWT.oid)
            .getOrElse(throw new RuntimeException("Application not found: " + hakemusJWT.oid))
            .personOid
        case _ => throw new RuntimeException("Invalid Json Web Token")
      }
    }

    def user = Oppija(getPersonOidFromSession)
    private val hakemusEditori = newEditor(this)

    val jwt = new JsonWebToken(appConfig.settings.hmacKey)

    def serverError = InternalServerError("errorType" -> "serverError")

    def returnHakemus(oid: Oid) = {
      hakemusRepository.getHakemus(oid) match {
        case Some(hakemusInfo) =>
          NonSensitiveHakemusInfo.apply(hakemusInfo, jwt.encode(HakemusJWT(hakemusInfo.hakemus.oid)))
        case _ =>
          logger.error("Hakemus not found! HakemusOid: " + oid)
          serverError
      }
    }

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
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      val visibleQuestions = newQuestions(updated)
      hakemusEditori.updateHakemus(updated) match {
        case Success(hakemus) => ActionResult(ResponseStatus(200), NonSensitiveHakemusInfo.sanitizeHakemus(hakemus, visibleQuestions), Map.empty)
        case Failure(e: ForbiddenException) => ActionResult(ResponseStatus(403), "errors" -> "Forbidden", Map.empty)
        case Failure(e: ValidationException) => ActionResult(ResponseStatus(400), e.validationErrors, Map.empty)
        case Failure(e: Throwable) => InternalServerError("error" -> "Internal service unavailable")
      }
    }

    get("/application/session") {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) => returnHakemus(hakemusJWT.oid)
        case Failure(e) => serverError
      }
    }

    get("/application/token/:token") {
      oppijanTunnistusService.validateToken(params("token")) match {
        case Success(hakemusOid) =>
          returnHakemus(hakemusOid)
        case Failure(e: InvalidTokenException) =>
          NotFound("errorType" -> "invalidToken")
        case Failure(exception) =>
          logger.error("Failed to validate token", exception)
          serverError
      }
    }

    post("/validate/:oid") {
      val muutos = Serialization.read[HakemusMuutos](request.body)
      hakemusEditori.validateHakemus(muutos) match {
        case Some(hakemusInfo) =>
          val info = NonSensitiveHakemusInfo.apply(hakemusInfo, jwt.encode(HakemusJWT(hakemusInfo.hakemus.oid))).hakemusInfo
          info.copy(questions = newQuestions(muutos))
        case _ => InternalServerError("error" -> "Internal service unavailable")
      }
    }

    def newHakukohteet(muutos: HakemusMuutos): List[HakutoiveData] = {
      val storedHakemus = hakemusRepository.getHakemus(params("oid")).get
      def isExistingHakukohde(hakukohde: HakutoiveData): Boolean = {
        storedHakemus.hakemus.preferences.exists(p => p.get(PREFERENCE_FRAGMENT_OPTION_ID) == hakukohde.get(PREFERENCE_FRAGMENT_OPTION_ID))
      }
      muutos.hakutoiveet.filter(p => !isExistingHakukohde(p))
    }

    def newQuestions(muutos: HakemusMuutos): List[QuestionNode] = {
      hakemusEditori.validateHakemus(muutos.copy(hakutoiveet = newHakukohteet(muutos))).get.questions
    }
  }

}
