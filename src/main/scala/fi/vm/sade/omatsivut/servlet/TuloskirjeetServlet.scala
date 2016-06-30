package fi.vm.sade.omatsivut.servlet

import java.io.File

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, HakemusRepositoryComponent}
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

trait TuloskirjeetServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    OppijanTunnistusComponent =>

  class TuloskirjeetServlet(val appConfig: AppConfig) extends OmatSivutServletBase {
    private val fileSystemUrl = appConfig.settings.tuloskirjeetFileSystemUrl;
    protected val applicationDescription = "REST API tuloskirjeille"
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

    def fetchTuloskirjeFromFileSystem(hakemusOid: String): Option[Array[Byte]] = {
      val path = s"$fileSystemUrl/$hakemusOid.pdf"
      val file = new File(path)
      log(s"Getting tuloskirje from $path")
      if(!file.exists()) {
        None
      } else {
        val source = scala.io.Source.fromFile(file)
        val byteArray = source.map(_.toByte).toArray
        source.close()
        Some(byteArray)
      }
    }

    get("/tuloskirje.pdf") {
      log(s"Getting tuloskirje.pdf")
      (for {
        token <- jwtAuthorize
        tuloskirje <- Try(fetchTuloskirjeFromFileSystem(token.oid))
      } yield {
        tuloskirje match {
          case Some(data) => Ok(tuloskirje, Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
          case None => InternalServerError("error" -> "Internal Server Error")
        }
      }).get
    }
  }

}