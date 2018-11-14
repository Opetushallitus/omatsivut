package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.groupemailer.GroupEmailComponent
import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.auditlog.Audit
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{ApplicationValidatorComponent, Fetch, HakemusInfo, HakemusRepositoryComponent, SpringContextComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.security.{AuthenticationRequiringServlet, SessionService}
import fi.vm.sade.omatsivut.vastaanotto.{Vastaanotto, VastaanottoComponent}
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.client.blaze
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._

import scala.util.{Failure, Success}

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with
        LomakeRepositoryComponent with
        HakemusRepositoryComponent with
        ValintatulosServiceComponent with
        ApplicationValidatorComponent with
        HakemusPreviewGeneratorComponent with
        SpringContextComponent with
        GroupEmailComponent with
        VastaanottoComponent with
        TranslationsComponent =>

  class ApplicationsServlet(val appConfig: AppConfig, val sessionService: SessionService) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport with AuthenticationRequiringServlet with HakemusEditoriUserContext {

    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)
    private val securitySettings = appConfig.settings.securitySettings
    private val blazeHttpClient = blaze.defaultClient
    private val casClient = new CasClient(securitySettings.casUrl, blazeHttpClient)
    private val serviceUrl = appConfig.settings.authenticationServiceConfig.url + "/"
    private val casParams = CasParams(serviceUrl, securitySettings.casUsername, securitySettings.casPassword)
    private val httpClient = CasAuthenticatingClient(casClient, casParams, blazeHttpClient, Some("omatsivut.omatsivut.backend"), "JSESSIONID")
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    get("/tuloskirje/:hakuOid") {
      val hakuOid = params("hakuOid")
      hakemusEditori.fetchTuloskirje(request, personOid(), hakuOid) match {
        case Some(tuloskirje) => Ok(tuloskirje, Map(
          "Content-Type" -> "application/octet-stream",
          "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
        case None => NotFound("error" -> "Not found")
      }
    }

    get("/") {
      val oid = personOid()
      hakemusEditori.fetchByPersonOid(request, oid, Fetch) match {
        case FullSuccess(hakemukset) =>
          Map("allApplicationsFetched" -> true, "applications" -> hakemukset)
        case PartialSuccess(hakemukset, exceptions) =>
          exceptions.foreach(logger.warn(s"Failed to fetch all applications for oid $oid",_))
          Map("allApplicationsFetched" -> false, "applications" -> hakemukset)
        case FullFailure(exceptions) =>
          exceptions.foreach(logger.error(s"Failed to fetch applications for oid $oid", _))
          throw exceptions.head
      }
    }

    put("/:oid") {
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      hakemusEditori.updateHakemus(request, updated) match {
        case Success(body) => ActionResult(ResponseStatus(200), body, Map.empty)
        case Failure(e: ForbiddenException) => ActionResult(ResponseStatus(403), "error" -> "Forbidden", Map.empty)
        case Failure(e: ValidationException) => ActionResult(ResponseStatus(400), e.validationErrors, Map.empty)
        case Failure(e) => InternalServerError("error" -> "Internal service unavailable")
      }
    }


    post("/validate/:oid") {
      val muutos = Serialization.read[HakemusMuutos](request.body)
      hakemusEditori.validateHakemus(request, muutos) match {
        case Some(hakemusInfo) => hakemusInfo
        case _ => InternalServerError("error" -> "Internal service unavailable")
      }
    }

    get("/preview/:oid") {
      newHakemusPreviewGenerator(language).generatePreview(request, personOid(), params("oid")) match {
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
      val vastaanotto = Serialization.read[Vastaanotto](request.body)

      hakemusEditori.fetchByHakemusOid(request, henkiloOid, hakemusOid, Fetch) match {
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
        case None =>
          logger.error(s"Vastaanotto failed because no application found for: henkiloOid $henkiloOid, hakemusOid $hakemusOid")
          NotFound("error" -> "Not found")
      }
    }
  }
}

