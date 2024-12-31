package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori._
import fi.vm.sade.hakemuseditori.hakemus.{Fetch, HakemusRepositoryComponent, SpringContextComponent}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.{AccessibleHtml, Pdf}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.{AuthenticationRequiringServlet, MigriJsonWebToken, SessionService}
import fi.vm.sade.omatsivut.vastaanotto.{Vastaanotto, VastaanottoComponent}
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with
        HakemusRepositoryComponent with
        ValintatulosServiceComponent with
        SpringContextComponent with
        VastaanottoComponent with
        TranslationsComponent =>

  class ApplicationsServlet(val appConfig: AppConfig, val sessionService: SessionService)
    extends OmatSivutServletBase
      with JsonFormats with JacksonJsonSupport with AuthenticationRequiringServlet with HakemusEditoriUserContext {

    private val migriJwt = new MigriJsonWebToken(appConfig.settings.hmacKeyMigri)

    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)

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
          logger.info("ApplicationsServlet get FullSuccess")
          Map(
            "allApplicationsFetched" -> true,
            "applications" -> hakemukset,
            "migriJwt" -> migriJwt.createMigriJWT(oid),
            "migriUrl" -> appConfig.settings.migriUrl
          )
        case PartialSuccess(hakemukset, exceptions) =>
          logger.info("ApplicationsServlet get PartialSuccess")
          exceptions.foreach(logger.warn(s"Failed to fetch all applications for oid $oid",_))
          Map(
            "allApplicationsFetched" -> false,
            "applications" -> hakemukset,
            "migriJwt" -> migriJwt.createMigriJWT(oid),
            "migriUrl" -> appConfig.settings.migriUrl
          )
        case FullFailure(exceptions) =>
          logger.info("ApplicationsServlet get FullFailure")
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
      // vanhojen hakemusten esikatselu ei enää onnistu omien sivujen kautta
      ActionResult(403, "error" -> "Forbidden", Map.empty)
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

