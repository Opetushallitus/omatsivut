package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.viestintapalvelu.{AccessibleHtml, Pdf, TuloskirjeComponent}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{ExpiredTokenException, InvalidTokenException, OppijanTunnistusComponent, OppijantunnistusMetadata}
import org.scalatra._

import scala.util.{Try}

trait TuloskirjeetServletContainer {
  this: HakemusEditoriComponent with
    TuloskirjeComponent with
    OppijanTunnistusComponent =>

  class TuloskirjeetServlet(val appConfig: AppConfig) extends OmatSivutServletBase with HakemusEditoriUserContext {

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

    get("/:token/tuloskirje.pdf") {
      val token = params("token")
      val hakuOid: Option[String] = params.get("hakuOid")
      (for {
        metadata <- oppijanTunnistusService.validateToken(token)
        tuloskirje <- Try(tuloskirjeService.fetchTuloskirje(
          request,
          metadata.hakuOid.getOrElse(
            if (hakuOid.isDefined) hakuOid.get
            else throw new RuntimeException(s"Haku OID not part of metadata for token ${token} nor given as parameter")),
          metadata.hakemusOid,
          Pdf))
      } yield {
        tuloskirje match {
          case Some(data: Array[Byte]) => Ok(data, Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
          case None => throw new NoSuchElementException
        }
      }).get
    }

    get("/:token/tuloskirje.html") {
      val token = params("token")
      val hakuOid: Option[String] = params.get("hakuOid")
      (for {
        metadata <- oppijanTunnistusService.validateToken(token)
        tuloskirje <- Try(tuloskirjeService.fetchTuloskirje(
          request,
          metadata.hakuOid.getOrElse(
            if (hakuOid.isDefined) hakuOid.get
            else throw new RuntimeException(s"Haku OID not part of metadata for token ${token} nor given as parameter")),
          metadata.hakemusOid,
          AccessibleHtml))
      } yield {
        tuloskirje match {
          case Some(data: Array[Byte]) =>
            response.setStatus(200)
            response.setContentType("text/html")
            response.setCharacterEncoding("utf-8")
            response.getWriter.println(new String(data))
            response.getWriter.flush()
          case None => throw new NoSuchElementException
        }
      }).get
    }
  }

}
