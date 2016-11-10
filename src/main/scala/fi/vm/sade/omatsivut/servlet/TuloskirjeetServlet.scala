package fi.vm.sade.omatsivut.servlet

import java.io.{FileInputStream, File}

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
import org.apache.commons.io.IOUtils
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport

import scala.util.{Failure, Success, Try}

trait TuloskirjeetServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    HakemusRepositoryComponent with
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

    def fetchTuloskirjeFromFileSystem(hakuOid: String, hakemusOid: String): Option[Array[Byte]] = {
      val path = s"$fileSystemUrl/$hakuOid/$hakemusOid.pdf"
      val file = new File(path)
      log(s"Getting tuloskirje from $path")
      if(!file.exists()) {
        None
      } else {
        val fileStream = new FileInputStream(file);
        val byteArray: Array[Byte] = IOUtils.toByteArray(fileStream)
        IOUtils.closeQuietly(fileStream)
        Some(byteArray)
      }
    }

    get("/:token/tuloskirje.pdf") {
      (for {
        hakemusOid <- oppijanTunnistusService.validateToken(params("token"))
        hakemusInfo <- hakemusRepository.getHakemus(hakemusOid, false)
        tuloskirje <- Try(fetchTuloskirjeFromFileSystem(hakemusInfo.hakemus.haku.oid, hakemusOid))
      } yield {
        tuloskirje match {
          case Some(data) => Ok(data, Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
          case None => InternalServerError("error" -> "Internal Server Error")
        }
      }).get
    }
  }

}