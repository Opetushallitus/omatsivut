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
import fi.vm.sade.hakemuseditori.viestintapalvelu.{AccessibleHtml, Pdf}
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.security.{AuthenticationRequiringServlet, JsonWebToken, MigriJsonWebToken, SessionService}
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

  class ApplicationsServlet(val appConfig: AppConfig, val sessionService: SessionService)
    extends OmatSivutServletBase
      with JsonFormats with JacksonJsonSupport with AuthenticationRequiringServlet with HakemusEditoriUserContext {

    private val migriJwt = new MigriJsonWebToken(appConfig.settings.hmacKeyMigri)

    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)
    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    get("/tuloskirje/:hakuOid") {
      val hakuOid = params("hakuOid")
      hakemusEditori.fetchTuloskirje(request, personOid(), hakuOid, AccessibleHtml) match {
        case Some(data) =>
          response.setStatus(200)
          response.setContentType("text/html")
          response.setCharacterEncoding("utf-8")
          response.getWriter.println(new String(data))
          response.getWriter.flush()
        case None => hakemusEditori.fetchTuloskirje(request, personOid(), hakuOid, Pdf) match {
          case Some(tuloskirje) => Ok(tuloskirje, Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> "attachment; filename=tuloskirje.pdf"))
          case None => NotFound("error" -> "Not found")
        }
      }
    }

    get("/") {
      val oid = personOid()
      hakemusEditori.fetchByPersonOid(request, oid, Fetch) match {
        case FullSuccess(hakemukset) =>
          Map(
            "allApplicationsFetched" -> true,
            "applications" -> hakemukset,
            "migriJwt" -> migriJwt.createMigriJWT(oid),
            "migriUrl" -> appConfig.settings.migriUrl
          )
        case PartialSuccess(hakemukset, exceptions) =>
          exceptions.foreach(logger.warn(s"Failed to fetch all applications for oid $oid",_))
          Map(
            "allApplicationsFetched" -> false,
            "applications" -> hakemukset,
            "migriJwt" -> migriJwt.createMigriJWT(oid),
            "migriUrl" -> appConfig.settings.migriUrl
          )
        case FullFailure(exceptions) =>
          exceptions.foreach(logger.error(s"Failed to fetch applications for oid $oid", _))
          throw exceptions.head
      }
    }

    put("/:oid") {
      // hakemuksen muokkaus ei enää onnistu omien sivujen kautta
      ActionResult(403, "error" -> "Forbidden", Map.empty)
    }


    post("/validate/:oid") {
      // hakemuksen muokkaus ei enää onnistu omien sivujen kautta
      ActionResult(403, "error" -> "Forbidden", Map.empty)
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

