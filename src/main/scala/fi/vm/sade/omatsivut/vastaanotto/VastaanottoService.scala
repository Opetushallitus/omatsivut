package fi.vm.sade.omatsivut.vastaanotto

import fi.oph.viestinvalitys.vastaanotto.model.ViestinvalitysBuilder

import java.text.SimpleDateFormat
import java.util.Calendar
import javax.servlet.http.HttpServletRequest
import fi.oph.viestinvalitys.ClientBuilder
import fi.vm.sade.hakemuseditori.HakemusEditoriComponent
import fi.vm.sade.hakemuseditori.auditlog.{Audit, SaveVastaanotto}
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.HakemusInfo
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.valintatulokset.domain.{VastaanotaSitovasti, VastaanottoAction}
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.scalatra.{ActionResult, Forbidden, InternalServerError, Ok}
import java.util.Optional

import scala.util.{Failure, Success, Try}

trait VastaanottoComponent {
  this: HakemusEditoriComponent =>

  def vastaanottoService(implicit language: Language): VastaanottoService

  class VastaanottoService(config: AppConfig)(implicit language: Language) extends JsonFormats {
    val OPH_PAAKAYTTAJA = "APP_VIESTINVALITYS_OPH_PAAKAYTTAJA"
    val OPH_ORGANISAATIO_OID = "1.2.246.562.10.00000000001"

    lazy val viestinvalitysClient =
      ClientBuilder.viestinvalitysClientBuilder()
        .withEndpoint(OphUrlProperties.url("viestinvalityspalvelu.url"))
        .withUsername(config.settings.securitySettings.casVirkailijaUsername)
        .withPassword(config.settings.securitySettings.casVirkailijaPassword)
        .withCasEndpoint(OphUrlProperties.url("cas.virkailija.url"))
        .withCallerId(AppConfig.callerId)
        .build()
    def vastaanota(request: HttpServletRequest, hakemusOid: String, hakukohdeOid: String, henkiloOid: String, vastaanotto: Vastaanotto, hakemus: HakemusInfo): ActionResult = {
      val hakuOid: String = hakemus.hakemus.haku.map(_.oid).getOrElse("")
      val email: Option[String] = hakemus.hakemus.email
      val henkiloOidHakemukselta = hakemus.hakemus.personOid
      if (!henkiloOid.equals(henkiloOidHakemukselta)) {
        logger.info(s"Tallennetaan vastaanotto hakemukselle $hakemusOid, henkilön session mukana kulkeva oid ($henkiloOid) " +
          s"on eri kuin hakemuksella oleva oid ($henkiloOidHakemukselta). Tehdään vastaanoton tallennus hakemuksen oidille.")
      }

      Try(valintatulosService.vastaanota(henkiloOidHakemukselta, hakemusOid, hakukohdeOid, vastaanotto.vastaanottoAction)) match {
        case Success(result) => {
          Audit.oppija.log(SaveVastaanotto(request, henkiloOidHakemukselta, hakemusOid, hakukohdeOid, hakuOid, vastaanotto.vastaanottoAction))
          if (result) {
            email match {
              case None => logger.error(s"""Vastaanottosähköpostia ei voitu lähettää, koska sähköpostiosoitetta ei löytynyt. HakemusOid: $hakemusOid""")
              case Some(email: String) => {
                vastaanottoService.sendVastaanottoEmail(hakuOid, vastaanotto, email)
              }
            }
            Ok(hakemus)
          } else {
            Forbidden("error" -> "Not receivable")
          }
        }
        case Failure(t) => {
          logger.error("failure in background service call", t)
          InternalServerError("error" -> "Background service failed")
        }
      }
    }

    private def sendVastaanottoEmail(hakuOid: String, vastaanotto: Vastaanotto, email: String): Unit = {
      try {
        val haku = tarjontaService.haku(hakuOid, language).get
        val subject = translations.getTranslation("message", "acceptEducation", "email", "subject")

        val dateFormat = new SimpleDateFormat(translations.getTranslation("message", "acceptEducation", "email", "dateFormat"))
        val dateAndTime = dateFormat.format(Calendar.getInstance().getTime)
        val vastaanottoActionString =
          if (haku.toisenasteenhaku && VastaanotaSitovasti.equals(vastaanotto.vastaanottoAction)) {
            vastaanotto.vastaanottoAction.toString + "ToinenAste"
          } else {
            vastaanotto.vastaanottoAction.toString
          }
        val answer = translations.getTranslation("message", "acceptEducation", "email", "tila", vastaanottoActionString)
        val aoInfoRow = List(answer, vastaanotto.tarjoajaNimi, vastaanotto.hakukohdeNimi).mkString(" - ")
        val body = translations.getTranslation("message", "acceptEducation", "email", "body")
          .format(dateAndTime, aoInfoRow)
          .replace("\n", "\n<br>")

      viestinvalitysClient.luoViesti(
      ViestinvalitysBuilder.viestiBuilder()
        .withOtsikko(subject)
        .withHtmlSisalto(body)
        .withKielet(language.toString)
        .withVastaanottajat(ViestinvalitysBuilder.vastaanottajatBuilder()
          .withVastaanottaja(Optional.empty(), email)
          .build())
        .withKayttooikeusRajoitukset(ViestinvalitysBuilder.kayttooikeusrajoituksetBuilder()
             .withKayttooikeus(OPH_PAAKAYTTAJA, OPH_ORGANISAATIO_OID)
             .build())
        .withLahettavaPalvelu("omatsivut")
        .withNormaaliPrioriteetti()
        .withLahettaja(Optional.empty(), "noreply@opintopolku.fi")
        .withSailytysAika(365)
        .build())
      } catch {
        case e: Exception => logger.error("Vastaanottosähköpostin lähetys epäonnistui: hakuOid / hakukohdeNimi / tarjoajaNimi / email : {} / {} / {} / {}", hakuOid, vastaanotto.hakukohdeNimi, vastaanotto.tarjoajaNimi, email, e)
      }
    }
  }
}

case class Vastaanotto(vastaanottoAction: VastaanottoAction, hakukohdeNimi: String = "", tarjoajaNimi: String = "")
