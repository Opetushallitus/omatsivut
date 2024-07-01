package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.auditlog.{Audit, SaveIlmoittautuminen}
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{FetchIfNoHetuOrToinenAste, HakemusInfo, HakemusRepositoryComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.domain.AnswerId
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Ilmoittautuminen
import fi.vm.sade.hakemuseditori.viestintapalvelu.{AccessibleHtml, Pdf}
import fi.vm.sade.omatsivut.NonSensitiveHakemusInfo.answerIds
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.oppijantunnistus.{ExpiredTokenException, InvalidTokenException, OppijanTunnistusComponent}
import fi.vm.sade.omatsivut.security.{HakemusJWT, JsonWebToken, MigriJsonWebToken}
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

case class InsecureHakemus(
  jsonWebToken: String,
  response: NonSensitiveHakemus
) extends InsecureResponse
case class InsecureHakemusInfo(
  jsonWebToken: String,
  response: NonSensitiveHakemusInfo,
  oiliJwt: String = null,
  migriJwt: String = null,
  migriUrl: String = null
) extends InsecureResponse

trait NonSensitiveApplicationServletContainer {
  this: HakemusRepositoryComponent with
    HakemusEditoriComponent with
    VastaanottoComponent with
    OppijanTunnistusComponent with
    OppijanumerorekisteriComponent with
    TarjontaComponent =>

  class NonSensitiveApplicationServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport with HakemusEditori with HakemusEditoriUserContext {
    override implicit val jsonFormats = JsonFormats.jsonFormats ++ List(new NonSensitiveHakemusSerializer, new NonSensitiveHakemusInfoSerializer)
    private val hakemusEditori = newEditor(this)
    private val jwt = new JsonWebToken(appConfig.settings.hmacKey)
    private val migriJwt = new MigriJsonWebToken(appConfig.settings.hmacKeyMigri)
    private val migriUrl = appConfig.settings.migriUrl

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
      val authHeader = request.getHeader("Authorization")
      authHeader match {
        case bearerMatch(jwtString) => jwt.decode(jwtString)
          .transform(Success(_), e => {
            logger.error(s"Authorization header decoding failed with request header ($authHeader)", e)
            Failure(new ForbiddenException(e.getMessage))
          })
        case _ =>
          logger.error(s"Authorization header handling failed with request header ($authHeader)")
          Failure(new UnauthorizedException("Invalid Authorization header"))
      }
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
        tuloskirje <- Try(fetchTuloskirje(request, token.personOid, hakuOid, AccessibleHtml))
      } yield {
        tuloskirje match {
          case Some(data) =>
            response.setStatus(200)
            response.setContentType("text/html")
            response.setCharacterEncoding("utf-8")
            response.getWriter.println(new String(data))
            response.getWriter.flush()
          case None =>
            fetchTuloskirje(request, token.personOid, hakuOid, Pdf) match {
              case Some(data) => Ok(data, Map(
                "Content-Type" -> "application/octet-stream",
                "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
              case None => InternalServerError("error" -> "Internal Server Error")
            }
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
      // hakemuksen muokkaus ei enää onnistu omien sivujen kautta
      Failure(new ForbiddenException("Forbidden"))
    }

    get("/applications/application/session") {
      (for {
        token <- jwtAuthorize
        hakemus <- fetchHakemus(token.oid, Some(token.personOid))
      } yield {
        val personOid = oppijanumerorekisteriService.henkilo(token.personOid).oid
        Ok(InsecureHakemusInfo(
          jwt.encode(token),
          new NonSensitiveHakemusInfo(hakemus, token.answersFromThisSession),
          oiliJwt = jwt.createOiliJwt(personOid),
          migriJwt = migriJwt.createMigriJWT(personOid),
          migriUrl = migriUrl
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
        hakemus: HakemusInfo <- fetchHakemus(metadata.hakemusOid, metadata.personOid)
      } yield {
        val personOid = oppijanumerorekisteriService.henkilo(hakemus.hakemus.personOid).oid
        Ok(InsecureHakemusInfo(
          jwt.encode(HakemusJWT(metadata.hakemusOid, Set(), hakemus.hakemus.personOid)),
          new NonSensitiveHakemusInfo(hakemus, Set()),
          oiliJwt = jwt.createOiliJwt(personOid),
          migriJwt = migriJwt.createMigriJWT(personOid),
          migriUrl = migriUrl
        ))
      }).get
    }

    post("/applications/validate/:oid") {
      // hakemuksen muokkaus ei enää onnistu omien sivujen kautta
      Failure(new ForbiddenException("Forbidden"))
    }
  }

}
