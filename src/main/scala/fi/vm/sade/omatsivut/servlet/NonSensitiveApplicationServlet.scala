package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.HakemusRepositoryComponent
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.HakutoiveData
import fi.vm.sade.hakemuseditori.hakemus.domain.{HakemusLike, HakemusMuutos}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.QuestionNode
import fi.vm.sade.hakemuseditori.user.Oppija
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
        case Success(hakemusJWT) => hakemusJWT.personOid
        case _ => throw new RuntimeException("Invalid Json Web Token")
      }
    }

    def user = Oppija(getPersonOidFromSession)
    private val hakemusEditori = newEditor(this)

    val jwt = new JsonWebToken(appConfig.settings.hmacKey)

    def serverError = InternalServerError("errorType" -> "serverError")

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
          val content: String = request.body
          val updated = Serialization.read[HakemusMuutos](content)
          val visibleQuestions = newQuestions(hakemusJWT.initialHakukohdeOids, updated)
          hakemusEditori.updateHakemus(updated) match {
            case Success(hakemus) => ActionResult(ResponseStatus(200), NonSensitiveHakemusInfo.sanitizeHakemus(hakemus, visibleQuestions), Map.empty)
            case Failure(e: ForbiddenException) => ActionResult(ResponseStatus(403), "errors" -> "Forbidden", Map.empty)
            case Failure(e: ValidationException) => ActionResult(ResponseStatus(400), e.validationErrors, Map.empty)
            case Failure(e: Throwable) => InternalServerError("error" -> "Internal service unavailable")
          }
        case Failure(_) => serverError
      }
    }

    get("/application/session") {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) =>
          val sensitiveHakemusInfo = hakemusRepository.getHakemus(hakemusJWT.oid).get
          val hakemus = sensitiveHakemusInfo.hakemus
          NonSensitiveHakemusInfo.apply(sensitiveHakemusInfo, newQuestions(hakemusJWT.initialHakukohdeOids,
            HakemusMuutos(hakemus.oid, hakemus.haku.oid, hakemus.preferences, Map.empty)))
        case Failure(e) => serverError
      }
    }

    get("/application/token/:token") {
      oppijanTunnistusService.validateToken(params("token")) match {
        case Success(hakemusOid) =>
          val personOid = applicationRepository
            .findStoredApplicationByOid(hakemusOid)
            .getOrElse(throw new RuntimeException("Application not found: " + hakemusOid))
            .personOid
          val nonSensitive = NonSensitiveHakemusInfo.apply(hakemusRepository.getHakemus(hakemusOid).get, List())
          Map("hakemusInfo" -> nonSensitive.hakemusInfo,
            "jsonWebToken" -> jwt.encode(
              HakemusJWT(
                hakemusOid,
                definedHakukohdes(nonSensitive.hakemusInfo.hakemus).map(p => p.get(PREFERENCE_FRAGMENT_OPTION_ID).get),
                personOid)
            )
          )
        case Failure(e: InvalidTokenException) =>
          NotFound("errorType" -> "invalidToken")
        case Failure(exception) =>
          logger.error("Failed to validate token", exception)
          serverError
      }
    }

    post("/validate/:oid") {
      getHakemusInfoFromBearerToken match {
        case Success(hakemusJWT) => {
          val muutos = Serialization.read[HakemusMuutos](request.body)
          hakemusEditori.validateHakemus(muutos) match {
            case Some(hakemusInfo) =>
              NonSensitiveHakemusInfo.apply(hakemusInfo, newQuestions(hakemusJWT.initialHakukohdeOids, muutos)).hakemusInfo
            case _ => InternalServerError("error" -> "Internal service unavailable")
          }
        }
        case Failure(_) => serverError
      }
    }

    def definedHakukohdes(hakemus: HakemusLike): List[HakutoiveData] = {
      hakemus.preferences.filter(p => p.contains(PREFERENCE_FRAGMENT_OPTION_ID))
    }

    def newHakukohteet(initialHakukohdeOids: List[Oid], muutos: HakemusMuutos): List[HakutoiveData] = {
      def isExistingHakukohde(hakukohde: HakutoiveData): Boolean = {
        initialHakukohdeOids.contains(hakukohde.get(PREFERENCE_FRAGMENT_OPTION_ID).get)
      }
      definedHakukohdes(muutos).filter(p => !isExistingHakukohde(p))
    }

    def newQuestions(initialHakukohdeOids: List[Oid], muutos: HakemusMuutos): List[QuestionNode] = {
      hakemusEditori.validateHakemus(muutos.copy(hakutoiveet = newHakukohteet(initialHakukohdeOids, muutos))).get.questions
    }
  }

}
