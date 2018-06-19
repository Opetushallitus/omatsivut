package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.{FetchIfNoHetuOrToinenAste, HakemusInfo, HakemusRepositoryComponent}
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.viestintapalvelu.TuloskirjeComponent
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{ExpiredTokenException, InvalidTokenException, OppijanTunnistusComponent, OppijantunnistusMetadata}
import javax.servlet.http.HttpServletRequest
import org.apache.commons.lang3.StringUtils
import org.scalatra._

import scala.util.{Failure, Success, Try}

trait TuloskirjeetServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    HakemusRepositoryComponent with
    TuloskirjeComponent with
    OppijanTunnistusComponent =>

  class TuloskirjeetServlet(val appConfig: AppConfig) extends OmatSivutServletBase with HakemusEditoriUserContext {
    protected val applicationDescription = "REST API tuloskirjeille"
    private val hakemusEditori = newEditor(this)

    def user(): Oppija = throw new RuntimeException("No user available")

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

    private def fetchHakemus(request: HttpServletRequest, hakemusOid: String, personOid: Option[String]): Try[HakemusInfo] = {
      personOid.map(hakemusEditori.fetchByHakemusOid(request, _, hakemusOid, FetchIfNoHetuOrToinenAste))
        .getOrElse(hakemusRepository.getHakemus(request, hakemusOid, FetchIfNoHetuOrToinenAste))
        .fold[Try[HakemusInfo]](Failure(new NoSuchElementException(s"Hakemus $hakemusOid not found")))(Success(_))
    }

    get("/:token/tuloskirje.pdf") {
      val token = params("token")
      (for {
        metadata <- oppijanTunnistusService.validateToken(token)
        hakemusInfo <- fetchHakemus(request, metadata.hakemusOid, metadata.personOid)
        tuloskirje <- Try(tuloskirjeService.fetchTuloskirje(
          request,
          hakemusInfo.hakemus.haku.oid,
          metadata.hakemusOid,
          resolvePersonOid(token, metadata, hakemusInfo)))
      } yield {
        tuloskirje match {
          case Some(data: Array[Byte]) => Ok(data, Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
          case None => throw new NoSuchElementException
        }
      }).get
    }
  }

  private def resolvePersonOid(token: String, metadata: OppijantunnistusMetadata, hakemusInfo: HakemusInfo): String = {
    metadata.personOid.getOrElse {
      if (StringUtils.isNotBlank(hakemusInfo.hakemus.personOid)) {
        hakemusInfo.hakemus.personOid
      } else {
        throw new IllegalArgumentException("Cannot find person oid when fetching tuloskirje " +
          s"for token $token of hakemus ${hakemusInfo.hakemus.oid} with metadata $metadata")
      }
    }
  }
}
