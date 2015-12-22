package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.hakemus.HakemusRepositoryComponent
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.NonSensitiveHakemus
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{InvalidTokenException, OppijanTunnistusComponent}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{InternalServerError, NotFound}

import scala.util.{Failure, Success}

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    OppijanTunnistusComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi muokata hakemusta heikosti tunnistautuneena"

    before() {
      contentType = formats("json")
    }

    get("/application/:token") {
      oppijanTunnistusService.validateToken(params("token")) match {
        case Success(hakemusOid) =>
          hakemusRepository.getHakemus(hakemusOid) match {
            case Some(hakemusInfo) =>
              val hakemus = hakemusInfo.hakemus
              NonSensitiveHakemus(hakemus.oid, hakemus.hakutoiveet)
            case _ =>
              logger.error("Token was valid but hakemus not found! Token: " + params("token") + ", hakemusOid: " + hakemusOid)
              InternalServerError("error" -> "Internal service unavailable")
          }
        case Failure(e: InvalidTokenException) =>
          NotFound("tokenValid" -> false)
        case Failure(exception) =>
          logger.error("Failed to validate token", exception)
          InternalServerError("error" -> "Failed to validate token")
      }
    }

  }

}
