package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.ataru.AtaruServiceComponent
import fi.vm.sade.groupemailer.GroupEmailComponent
import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.auditlog.AuditLoggerComponent
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.HakemusMuutos
import fi.vm.sade.hakemuseditori.hakemus.{ApplicationValidatorComponent, HakemusRepositoryComponent, SpringContextComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.valintatulokset.domain._
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._

import scala.util.{Failure, Success}

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with
        LomakeRepositoryComponent with
        AtaruServiceComponent with
        HakemusRepositoryComponent with
        ValintatulosServiceComponent with
        ApplicationValidatorComponent with
        HakemusPreviewGeneratorComponent with
        SpringContextComponent with
        AuditLoggerComponent with
        GroupEmailComponent with
        VastaanottoEmailContainer with
        TranslationsComponent =>

  class ApplicationsServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport with AuthenticationRequiringServlet with HakemusEditoriUserContext {

    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)

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

    get("/") {
      { ataruService.findApplications(personOid()) ::: hakemusEditori.fetchByPersonOid(personOid()) }.sortBy[Option[Long]](_.hakemus.received).reverse
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

      hakemusRepository.getHakemus(hakemusOid)
        .orElse(ataruService.findApplications(henkiloOid).find(_.hakemus.oid == hakemusOid)) match {
        case Some(hakemus) if tarjontaService.haku(hakemus.hakemus.haku.oid, Language.fi).exists(_.published) =>
          vastaanota(
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

