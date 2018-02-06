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
import fi.vm.sade.utils.http.DefaultHttpClient
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
        VastaanottoEmailContainer with
        TranslationsComponent =>

  class ApplicationsServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JsonFormats with JacksonJsonSupport with AuthenticationRequiringServlet with HakemusEditoriUserContext {

    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)
    private val httpClient = DefaultHttpClient
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
      val pOid: String = personOid()
      val masterRequest = httpClient.httpGet(OphUrlProperties.url("oppijanumerorekisteri-service.henkilo-master", pOid))
        .header("Caller-Id", "omatsivut.omatsivut.backend")

      val allOids: List[String] = masterRequest.responseWithHeaders() match {
        case (200, _, masterOid) =>
          val slaveRequest = httpClient.httpGet(OphUrlProperties.url("oppijanumerorekisteri-service.henkilo-slaves", masterOid))
            .header("Caller-Id", "omatsivut.omatsivut.backend")
          slaveRequest.responseWithHeaders() match {
            case (200, _, slaveOidResult) =>
              List(masterOid) ++ parse(slaveOidResult).extract[List[String]]
            case (code,_, slaveOidFailure) =>
              logger.error("Failed to fetch slave OIDs for user oid {}, response was {}, {}", masterOid, Integer.toString(code), slaveOidFailure)
              List(masterOid)
          }
        case (code,_, resultString) =>
          logger.error("Failed to fetch master OID for user oid {}, response was {}, {}", pOid, Integer.toString(code), resultString)
          List(pOid)
      }

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

