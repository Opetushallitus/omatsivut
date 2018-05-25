package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.auditlog.{Audit, SaveIlmoittautuminen}
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{FetchIfNoHetuOrToinenAste, HakemusInfo, HakemusRepositoryComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Ilmoittautuminen
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.answerIds
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{ExpiredTokenException, InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken}
import fi.vm.sade.omatsivut.vastaanotto.{Vastaanotto, VastaanottoComponent}
import fi.vm.sade.omatsivut.{NonSensitiveHakemus, NonSensitiveHakemusInfo, NonSensitiveHakemusInfoSerializer, NonSensitiveHakemusSerializer}
import org.json4s._
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport

import scala.util.{Failure, Success, Try}

sealed trait InsecureResponse {
  def jsonWebToken: String
}

case class InsecureHakemus(jsonWebToken: String, response: NonSensitiveHakemus) extends InsecureResponse
case class InsecureHakemusInfo(jsonWebToken: String, response: NonSensitiveHakemusInfo, oiliJwt: String = null) extends InsecureResponse

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    VastaanottoComponent with
    OppijanTunnistusComponent with
    TarjontaComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport with HakemusEditori with HakemusEditoriUserContext {
    override implicit val jsonFormats = JsonFormats.jsonFormats ++ List(new NonSensitiveHakemusSerializer, new NonSensitiveHakemusInfoSerializer)
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi muokata hakemusta heikosti tunnistautuneena"
    private val hakemusEditori = newEditor(this)
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

    private def newAnswersFromTheSession(update: HakemusMuutos, hakemusJWT: HakemusJWT): Set[AnswerId] = {
      val nonPersistedAnswers = answerIds(update.answers)
      hakemusJWT.answersFromThisSession ++ nonPersistedAnswers
    }

    private def fetchHakemus(hakemusOid: String, personOid: Option[String]): Try[HakemusInfo] = {
      personOid.map(hakemusEditori.fetchByHakemusOid(request, _, hakemusOid, FetchIfNoHetuOrToinenAste))
        .getOrElse(hakemusRepository.getHakemus(request, hakemusOid, FetchIfNoHetuOrToinenAste))
        .fold[Try[HakemusInfo]](Failure(new NoSuchElementException(s"Hakemus $hakemusOid not found")))(h => Success(h.withoutKelaUrl))
    }

    before() {
      contentType = formats("json")
    }

    get("/applications/tuloskirje/:hakuOid") {
      val hakuOid = params("hakuOid")
      (for {
        token <- jwtAuthorize
        tuloskirje <- Try(fetchTuloskirje(request, token.personOid, hakuOid))
      } yield {
        tuloskirje match {
          case Some(data) => Ok(tuloskirje, Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
          case None => InternalServerError("error" -> "Internal Server Error")
        }
      }).get
    }

    post("/ilmoittaudu") {
      val ilmoittautuminen = parsedBody.extract[Ilmoittautuminen]
      val hakuOid = params("hakuOid")
      val hakemusOid = params("hakemusOid")

      ilmoittautuminen.muokkaaja = user().oid
      val bool = valintatulosService.ilmoittaudu(hakuOid, hakemusOid, ilmoittautuminen)
      Audit.oppija.log(SaveIlmoittautuminen(request, hakuOid, hakemusOid, ilmoittautuminen, bool))
    }

    put("/applications/:oid") {
      (for {
        token <- jwtAuthorize
        update <- Try(Serialization.read[HakemusMuutos](request.body))
        newAnswers <- Success(newAnswersFromTheSession(update, token))
        updatedHakemus <- hakemusEditori.updateHakemus(request, NonSensitiveHakemusInfo.sanitizeHakemusMuutos(update, newAnswers))
      } yield {
        Ok(InsecureHakemus(jwt.encode(HakemusJWT(token.oid, newAnswers, token.personOid)),
          new NonSensitiveHakemus(updatedHakemus, newAnswers)))
      }).get
    }

    get("/applications/application/session") {
      (for {
        token <- jwtAuthorize
        hakemus <- fetchHakemus(token.oid, Some(token.personOid))
      } yield {
        Ok(InsecureHakemusInfo(
          jwt.encode(token),
          new NonSensitiveHakemusInfo(hakemus, token.answersFromThisSession),
          oiliJwt = jwt.createOiliJwt(token.personOid)
        ))
      }).get
    }

    post("/applications/vastaanota/:hakemusOid/hakukohde/:hakukohdeOid") {
      val hakemusOid = params("hakemusOid")
      val hakukohdeOid = params("hakukohdeOid")
      val henkiloOid = getPersonOidFromSession
      val vastaanotto = Serialization.read[Vastaanotto](request.body)

      hakemusEditori.fetchByHakemusOid(request, henkiloOid, hakemusOid, FetchIfNoHetuOrToinenAste) match {
        case None => NotFound("error" -> "Not found")
        case Some(hakemus) => {
          vastaanottoService.vastaanota(
            request,
            hakemusOid,
            hakukohdeOid,
            henkiloOid,
            vastaanotto,
            hakemus
          )
        }
      }
    }

    get("/applications/application/token/:token") {
      (for {
        metadata <- oppijanTunnistusService.validateToken(params("token"))
        hakemus <- fetchHakemus(metadata.hakemusOid, metadata.personOid)
      } yield {
        Ok(InsecureHakemusInfo(
          jwt.encode(HakemusJWT(metadata.hakemusOid, Set(), hakemus.hakemus.personOid)),
          new NonSensitiveHakemusInfo(hakemus, Set()),
          oiliJwt = jwt.createOiliJwt(hakemus.hakemus.personOid)
        ))
      }).get
    }

    post("/applications/validate/:oid") {
      (for {
        token <- jwtAuthorize
        update <- Try(Serialization.read[HakemusMuutos](request.body))
        validatedHakemus <- hakemusEditori.validateHakemus(update)
          .fold[Try[HakemusInfo]](Failure(new RuntimeException))(Success(_))
      } yield {
        Ok(InsecureHakemusInfo(jwt.encode(token),
          new NonSensitiveHakemusInfo(validatedHakemus, newAnswersFromTheSession(update, token))))
      }).get
    }
  }

}
