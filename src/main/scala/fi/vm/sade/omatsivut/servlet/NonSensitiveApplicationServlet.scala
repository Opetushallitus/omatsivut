package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.hakemus.HakemusRepositoryComponent
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.NonSensitiveHakemus
import fi.vm.sade.omatsivut.NonSensitiveHakemus.Oid
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{InternalServerError, NotFound}

import scala.util.{Failure, Success}

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    OppijanTunnistusComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi muokata hakemusta heikosti tunnistautuneena"

    val jwt = new JsonWebToken(appConfig.settings.hmacKey)

    def serverError = InternalServerError("errorType" -> "serverError")

    def returnHakemus(oid: Oid) = {
      hakemusRepository.getHakemus(oid) match {
        case Some(hakemusInfo) =>
          val hakemus = hakemusInfo.hakemus
          NonSensitiveHakemus(hakemus.oid, hakemus.hakutoiveet, jwt.encode(HakemusJWT(hakemus.oid)))
        case _ =>
          logger.error("Hakemus not found! HakemusOid: " + oid)
          serverError
      }
    }

    before() {
      contentType = formats("json")
    }

    get("/application/session") {
      val bearerMatch = """Bearer (.+)""".r
      response.getHeader("Authorization") match {
        case bearerMatch(token) =>
          jwt.decode(token) match {
            case Success(hakemus) =>
              returnHakemus(hakemus.oid)
            case Failure(e) => serverError
          }
        case _ => serverError
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

  }

}
