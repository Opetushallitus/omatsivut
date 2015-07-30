package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.{HakemusEditoriUserContext, UpdateResult, HakemusEditoriComponent}
import fi.vm.sade.hakemuseditori.auditlog.{AuditLogger, AuditLoggerComponent, SaveVastaanotto}
import fi.vm.sade.hakemuseditori.hakemus.domain.{HakemusMuutos, Hakemus}
import fi.vm.sade.hakemuseditori.hakemus.{SpringContextComponent, HakemusInfo, ApplicationValidatorComponent, HakemusRepositoryComponent}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.omatsivut.hakemuspreview.HakemusPreviewGeneratorComponent
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Vastaanotto
import fi.vm.sade.omatsivut.valintarekisteri.ValintaRekisteriComponent
import org.json4s.jackson.Serialization
import org.scalatra._
import org.scalatra.json._
import scala.util.{Failure, Success}

trait ApplicationsServletContainer {
  this: HakemusEditoriComponent with LomakeRepositoryComponent with
    HakemusRepositoryComponent with
    ValintatulosServiceComponent with
    ValintaRekisteriComponent with
    ApplicationValidatorComponent with
    HakemusPreviewGeneratorComponent with
    SpringContextComponent with
    AuditLoggerComponent =>

  class ApplicationsServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with AuthenticationRequiringServlet with HakemusEditoriUserContext {
    def user = Oppija(personOid())
    private val hakemusEditori = newEditor(this)

    protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla voi hakea ja muokata hakemuksia ja omia tietoja"

    before() {
      contentType = formats("json")
    }

    get("/") {
      hakemusEditori.fetchByPersonOid(personOid())
    }


    put("/:oid") {
      val content: String = request.body
      val updated = Serialization.read[HakemusMuutos](content)
      val response: Option[ActionResult] = hakemusEditori.updateHakemus(updated)
        .map { case UpdateResult(status, body) => ActionResult(ResponseStatus(status), body, Map.empty)}
      response.getOrElse(InternalServerError("error" -> "Internal service unavailable"))
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

    post("/vastaanota/:hakuOid/:hakemusOid") {
      val hakemusOid = params("hakemusOid")
      val hakuOid = params("hakuOid")
      val henkilo = personOid()
      if (!applicationRepository.exists(henkilo, hakemusOid)) {
        NotFound("error" -> "Not found")
      } else {
        val clientVastaanotto = Serialization.read[ClientSideVastaanotto](request.body)
        val vastaanotto = Vastaanotto(clientVastaanotto.hakukohdeOid, clientVastaanotto.tila, henkilo, "Muokkaus Omat Sivut -palvelussa")
        val ret = if(valintaRekisteriService.isEnabled) {
          valintaRekisteriService.vastaanota(henkilo, vastaanotto.hakukohdeOid, henkilo)
        } else {
          valintatulosService.vastaanota(hakemusOid, hakuOid, vastaanotto)
        }
        if(ret) {
          auditLogger.log(SaveVastaanotto(personOid(), hakemusOid, hakuOid, vastaanotto))
          hakemusRepository.getHakemus(hakemusOid) match {
            case Some(hakemus) => hakemus
            case _ => NotFound("error" -> "Not found")
          }
        } else {
          InternalServerError("error" -> "Not receivable")
        }
      }
    }
  }
}

case class ClientSideVastaanotto(hakukohdeOid: String, tila: String)

