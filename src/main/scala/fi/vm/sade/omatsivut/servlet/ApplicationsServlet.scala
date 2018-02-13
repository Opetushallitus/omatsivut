package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.groupemailer.GroupEmailComponent
import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{ApplicationValidatorComponent, Fetch, HakemusRepositoryComponent, SpringContextComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.valintatulokset.domain._
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.{Header, Headers, Request, Uri}
import org.http4s.client.blaze
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._

import scala.util.{Failure, Success}
import scalaz.concurrent.Task

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with
        LomakeRepositoryComponent with
        HakemusRepositoryComponent with
        ValintatulosServiceComponent with
        ApplicationValidatorComponent with
        HakemusPreviewGeneratorComponent with
        SpringContextComponent with
        GroupEmailComponent with
        VastaanottoEmailContainer with
        TranslationsComponent =>

  class ApplicationsServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport with AuthenticationRequiringServlet with HakemusEditoriUserContext {

    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)
    private val securitySettings = appConfig.settings.securitySettings
    private val blazeHttpClient = blaze.defaultClient
    private val casClient = new CasClient(securitySettings.casUrl, blazeHttpClient)
    //private val httpClient = DefaultHttpClient
    private val serviceUrl = appConfig.settings.authenticationServiceConfig.url + "/"
    private val casParams = CasParams(serviceUrl, securitySettings.casUsername, securitySettings.casPassword)
    private val httpClient = CasAuthenticatingClient(casClient, casParams, blazeHttpClient, Some("omatsivut.omatsivut.backend"), "JSESSIONID")
    private val callerIdHeader = Header("Caller-Id", "omatsivut.omatsivut.backend")
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    get("/tuloskirje/:hakuOid") {
      val hakuOid = params("hakuOid")
      hakemusEditori.fetchTuloskirje(personOid(), hakuOid) match {
        case Some(tuloskirje) => Ok(tuloskirje, Map(
          "Content-Type" -> "application/octet-stream",
          "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
        case None => NotFound("error" -> "Not found")
      }
    }

    private def uriFromString(url: String): Uri = {
      Uri.fromString(url).toOption.get
    }
    private def runHttp[ResultType](request: Request)(decoder: (Int, String, Request) => ResultType): Task[ResultType] = {
      httpClient.fetch(request)(r => r.as[String].map(body => decoder(r.status.code, body, request)))
    }

    get("/") {
      val timeout = 1000*30L
      val pOid: String = personOid()

      val masterRequest: Request = Request(
        uri = uriFromString(OphUrlProperties.url("oppijanumerorekisteri-service.henkilo-master", pOid)),
        headers = Headers(callerIdHeader))

      val masterOid: String = runHttp[Option[String]](masterRequest) {
        case (200, resultString, _) => Some(resultString)
        case (code, responseString, _) =>
          logger.error("Failed to fetch master oid for user oid {}, response was {}, {}", pOid, Integer.toString(code), responseString)
          None
      }.runFor(timeoutInMillis = timeout).getOrElse(pOid)

      val slaveRequest: Request = Request(
        uri = uriFromString(OphUrlProperties.url("oppijanumerorekisteri-service.henkilo-slaves", masterOid)),
        headers = Headers(callerIdHeader))

      val allOids: List[String] = runHttp(slaveRequest) {
        case (200, resultString, _) =>
          List(masterOid) ++ parse(resultString).extract[List[String]]
        case (code, responseString, _) =>
          logger.error("Failed to fetch slave OIDs for user oid {}, response was {}, {}", masterOid, Integer.toString(code), responseString)
          List(masterOid)
      }.runFor(timeoutInMillis = timeout)

      var allSuccess = true
      val allHakemukset = allOids.flatMap(oid => {
        hakemusEditori.fetchByPersonOid(oid, Fetch) match {
          case FullSuccess(hakemukset) => hakemukset
          case PartialSuccess(partialHakemukset, exceptions) =>
            exceptions.foreach(logger.warn("Failed to fetch all applications", _))
            allSuccess = false
            partialHakemukset
          case FullFailure(exceptions) =>
            exceptions.foreach(logger.error("Failed to fetch applications", _))
            allSuccess = false
            List.empty
        }
      })

      Map("allApplicationsFetched" -> allSuccess, "applications" -> allHakemukset)
    }

    put("/:oid") {
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      hakemusEditori.updateHakemus(updated) match {
        case Success(body) => ActionResult(ResponseStatus(200), body, Map.empty)
        case Failure(e: ForbiddenException) => ActionResult(ResponseStatus(403), "error" -> "Forbidden", Map.empty)
        case Failure(e: ValidationException) => ActionResult(ResponseStatus(400), e.validationErrors, Map.empty)
        case Failure(e) => InternalServerError("error" -> "Internal service unavailable")
      }
    }


    post("/validate/:oid") {
      val muutos = Serialization.read[HakemusMuutos](request.body)

      hakemusEditori.validateHakemus(muutos) match {
        case Some(hakemusInfo) => hakemusInfo
        case _ => InternalServerError("error" -> "Internal service unavailable")
      }
    }

    get("/preview/:oid") {
      newHakemusPreviewGenerator(language).generatePreview(personOid(), params("oid")) match {
        case Some(previewHtml) =>
          contentType = formats("html")
          Ok(previewHtml)
        case None =>
          NotFound("error" -> "Not found")
      }
    }

    post("/vastaanota/:hakemusOid/hakukohde/:hakukohdeOid") {
      val hakemusOid = params("hakemusOid")
      val hakukohdeOid = params("hakukohdeOid")
      val henkiloOid = personOid()

      hakemusEditori.fetchByHakemusOid(henkiloOid, hakemusOid, Fetch) match {
        case Some(hakemus) => vastaanota(
          hakemusOid,
          hakukohdeOid,
          hakemus.hakemus.haku.oid,
          henkiloOid,
          request.body,
          hakemus.hakemus.email,
          () => Some(hakemus)
        )
        case None => NotFound("error" -> "Not found")
      }
    }
  }
}

case class ClientSideVastaanotto(vastaanottoAction: VastaanottoAction, hakukohdeNimi: String = "", tarjoajaNimi: String = "")

