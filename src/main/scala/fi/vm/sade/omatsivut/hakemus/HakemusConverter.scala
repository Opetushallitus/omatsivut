package fi.vm.sade.omatsivut.hakemus

import java.util
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.tarjonta.Haku
import fi.vm.sade.omatsivut.valintatulokset.ValintatulosServiceComponent
import scala.collection.JavaConverters._
import scala.util.Try
import fi.vm.sade.omatsivut.valintatulokset.Vastaanottoaikataulu
import org.joda.time.LocalDateTime
import scala.collection.JavaConversions._

trait HakemusConverterComponent {
  this: ValintatulosServiceComponent =>

  val hakemusConverter: HakemusConverter

  class HakemusConverter {
    val educationPhaseKey = OppijaConstants.PHASE_EDUCATION
    val baseEducationKey = OppijaConstants.ELEMENT_ID_BASE_EDUCATION
    val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

    def convertToHakemus(applicationSystem: ApplicationSystem, haku: Haku, application: Application)(implicit lang: Language.Language) = {
      val koulutusTaustaAnswers: util.Map[String, String] = application.getAnswers.get(educationPhaseKey)
      val receivedTime =  application.getReceived.getTime
      val hakutoiveet = convertHakuToiveet(application)
      Hakemus(
        application.getOid,
        receivedTime,
        Option(application.getUpdated).map(_.getTime).getOrElse(receivedTime),
        tila(applicationSystem.getId, haku, application, hakutoiveet),
        hakutoiveet,
        haku,
        EducationBackground(koulutusTaustaAnswers.get(baseEducationKey), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false)),
        application.clone().getAnswers.toMap.mapValues { phaseAnswers => phaseAnswers.toMap },
        AttachmentConverter.requiresAdditionalInfo(applicationSystem.getAdditionalInformationElements.asScala.toList, application)
      )
    }

    def tila(applicationSystemId: String, haku: Haku, application: Application, hakutoiveet: List[Hakutoive])(implicit lang: Language.Language): HakemuksenTila = {
      if (isPostProcessing(application)) {
        PostProcessing()
      } else {
        if (!haku.applicationPeriods.head.active) {
          val valintatulos = convertToValintatulos(applicationSystemId, application, hakutoiveet)
          HakuPaattynyt(valintatulos = valintatulos)
        } else {
          application.getState.toString match {
            case "ACTIVE" => Active()
            case "PASSIVE" => Passive()
            case "INCOMPLETE" => Incomplete()
            case "SUBMITTED" => Submitted()
            case x => {
              throw new RuntimeException("Unexpected state for application " + application.getOid + ": " + x)
            }
          }
        }
      }
    }

    private def isKesken(hakutoiveenValintatulos: HakutoiveenValintatulos) = {
      hakutoiveenValintatulos.tila  == HakutoiveenValintatulosTila.KESKEN ||
      hakutoiveenValintatulos.tila == HakutoiveenValintatulosTila.VARALLA
    }

    private def isHyvaksytty(hakutoiveenValintatulos: HakutoiveenValintatulos) = {
      hakutoiveenValintatulos.tila  == HakutoiveenValintatulosTila.HYVAKSYTTY ||
      hakutoiveenValintatulos.tila == HakutoiveenValintatulosTila.HARKINNANVARAISESTI_HYVAKSYTTY ||
      hakutoiveenValintatulos.tila == HakutoiveenValintatulosTila.VARASIJALTA_HYVAKSYTTY
    }

    private def vastaanottotieto(valintatulokset: List[HakutoiveenValintatulos]) = {
      val valmisIndex = valintatulokset.indexWhere(_.vastaanottotila != ResultState.KESKEN)
      if (valmisIndex >= 0) {
        if (valintatulokset(valmisIndex).isPeruuntunut) {
          valintatulokset.slice(0, valmisIndex).find(isKesken(_)) match {
            case Some(kesken) => None // jos jokin yläpuolella on varalla
            case _ => Some(valintatulokset(valmisIndex))
          }
        } else
          Some(valintatulokset(valmisIndex))
      } else {
        None
      }
    }

    private def isVastaanotettavissa(hakutoiveenValintatulos: HakutoiveenValintatulos) = {
      hakutoiveenValintatulos.vastaanotettavuustila == VastaanotettavuusTila.VASTAANOTETTAVISSA_EHDOLLISESTI ||
      hakutoiveenValintatulos.vastaanotettavuustila == VastaanotettavuusTila.VASTAANOTETTAVISSA_SITOVASTI
    }

    private def convertToValintatulos(applicationSystemId: String, application: Application, hakutoiveet: List[Hakutoive])(implicit lang: Language.Language): Option[Valintatulos] = {
      def findKoulutus(oid: String): Koulutus = {
        hakutoiveet.find(_.get("Koulutus-id") == Some(oid)).map{ hakutoive => Koulutus(oid, hakutoive("Koulutus"))}.getOrElse(Koulutus(oid, oid))
      }

      def findOpetuspiste(oid: String): Opetuspiste = {
        hakutoiveet.find(_.get("Opetuspiste-id") == Some(oid)).map{ hakutoive => Opetuspiste(oid, hakutoive("Opetuspiste"))}.getOrElse(Opetuspiste(oid, oid))
      }
      valintatulosService.getValintatulos(application.getOid, applicationSystemId).map { valintaTulos =>
        Valintatulos(valintaTulos.hakutoiveet.map { hakutoiveenTulos =>
          HakutoiveenValintatulos(
            findKoulutus(hakutoiveenTulos.hakukohdeOid),
            findOpetuspiste(hakutoiveenTulos.tarjoajaOid),
            HakutoiveenValintatulosTila.withName(hakutoiveenTulos.valintatila),
            convertToResultsState(hakutoiveenTulos),
            VastaanotettavuusTila.withName(hakutoiveenTulos.vastaanotettavuustila),
            convertToVastaanotettavuusAsti(valintaTulos.aikataulu, hakutoiveenTulos),
            hakutoiveenTulos.viimeisinValintatuloksenMuutos .map(_.getTime),
            hakutoiveenTulos.ilmoittautumistila,
            hakutoiveenTulos.jonosija,
            hakutoiveenTulos.varasijojaTaytetaanAsti.map(_.getTime),
            hakutoiveenTulos.varasijanumero,
            hakutoiveenTulos.tilanKuvaukset.get(lang.toString.toUpperCase)
          )
        })
      }
    }

    private def convertToVastaanotettavuusAsti(aikataulu: Option[Vastaanottoaikataulu], hakutoiveenTulos: fi.vm.sade.omatsivut.valintatulokset.HakutoiveenValintatulos): Option[Long] = {
      aikataulu match {
        case None => None
        case Some(aikataulu) => {
          val bufferDeadline = hakutoiveenTulos.viimeisinValintatuloksenMuutos.map(new LocalDateTime(_).plusDays(aikataulu.vastaanottoBufferDays.getOrElse(0)).toDate())
          (aikataulu.vastaanottoEnd , bufferDeadline) match {
            case (Some(end), Some(buffer)) => Some((if(buffer.after(end)) buffer else end).getTime())
            case (end, _) => end.map(_.getTime())
          }
        }
      }
    }

    private def convertToResultsState(hakutoiveenTulos: fi.vm.sade.omatsivut.valintatulokset.HakutoiveenValintatulos) = {
      hakutoiveenTulos.vastaanottotila  match {
        case None => ResultState.KESKEN
        case Some(tulos) => {
          ResultState.withName(tulos)
        }
      }
    }

    private def isPostProcessing(application: Application): Boolean = {
      val state = application.getRedoPostProcess
      !(state == Application.PostProcessingState.DONE || state == null)
    }

    private def convertHakuToiveet(application: Application): List[Hakutoive] = {
      HakutoiveetConverter.convertFromAnswers(application.getAnswers.toMap.mapValues(_.toMap))
    }

  }
}
