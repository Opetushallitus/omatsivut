package fi.vm.sade.omatsivut.hakemus

import java.util
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.hakemus.domain.Hakemus._
import fi.vm.sade.omatsivut.hakemus.domain._
import fi.vm.sade.omatsivut.haku.domain.Haku
import fi.vm.sade.omatsivut.valintatulokset.ValintatulosServiceComponent

import scala.collection.JavaConversions._
import scala.util.Try

trait HakemusConverterComponent {
  this: ValintatulosServiceComponent =>

  val hakemusConverter: HakemusConverter

  class HakemusConverter {
    val educationPhaseKey = OppijaConstants.PHASE_EDUCATION
    val baseEducationKey = OppijaConstants.ELEMENT_ID_BASE_EDUCATION
    val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

    def convertToHakemus(applicationSystem: ApplicationSystem, haku: Haku, application: Application) = {
      val koulutusTaustaAnswers: util.Map[String, String] = application.getAnswers.get(educationPhaseKey)
      val receivedTime =  application.getReceived.getTime
      val hakutoiveet = convertHakuToiveet(application)
      Hakemus(
        application.getOid,
        receivedTime,
        Option(application.getUpdated).map(_.getTime).getOrElse(receivedTime),
        tila(applicationSystem, haku, application, hakutoiveet),
        hakutoiveet,
        haku,
        EducationBackground(koulutusTaustaAnswers.get(baseEducationKey), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false)),
        application.clone().getAnswers.toMap.mapValues { phaseAnswers => phaseAnswers.toMap },
        AttachmentConverter.requiresAdditionalInfo(applicationSystem, application)
      )
    }

    def tila(applicationSystem: ApplicationSystem, haku: Haku, application: Application, hakutoiveet: List[Hakutoive]): HakemuksenTila = {
      if (isPostProcessing(application)) {
        PostProcessing()
      } else {
        if (!haku.applicationPeriods.head.active) {
          val valintatulos = convertToValintatulos(applicationSystem, application, hakutoiveet)
          HakuPaattynyt(valintatulos = valintatulos, resultStatus = resultStatus(valintatulos))
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

    private def resultStatus(valintatulos: Option[Valintatulos]): Option[ResultStatus] = {
      valintatulos.flatMap(tulos => {
        tulos.hakutoiveet.find(hasVastaanottotieto(_)) match {
          case Some(vastaanotettu) => Some(ResultStatus(vastaanotettu.vastaanottotila, Some(vastaanotettu.opetuspiste.name + " - " + vastaanotettu.koulutus.name)))
          case None => {
            tulos.hakutoiveet.find(isVastaanotettavissa(_)) match {
              case Some(vastaanotettavissa) => None
              case None => {
                if(tulos.hakutoiveet.exists(isKesken(_)) || tulos.hakutoiveet.exists(isHyvaksytty(_))) {
                  Some(ResultStatus())
                }
                else {
                  Some(ResultStatus(ResultState.withName(tulos.hakutoiveet.head.tila.toString()), None))
                }
              }
            }
          }
        }
      })
    }

    private def isKesken(hakutoiveenValintatulos: HakutoiveenValintatulos) = {
      hakutoiveenValintatulos.tila  == HakutoiveenValintatulosTila.KESKEN ||
      hakutoiveenValintatulos.tila == HakutoiveenValintatulosTila.VARALLA
    }

    private def isHyvaksytty(hakutoiveenValintatulos: HakutoiveenValintatulos) = {
      hakutoiveenValintatulos.tila  == HakutoiveenValintatulosTila.HYVAKSYTTY ||
      hakutoiveenValintatulos.tila == HakutoiveenValintatulosTila.HARKINNANVARAISESTI_HYVAKSYTTY
    }

    private def hasVastaanottotieto(hakutoiveenValintatulos: HakutoiveenValintatulos) = {
      hakutoiveenValintatulos.vastaanottotila == ResultState.VASTAANOTTANUT ||
      hakutoiveenValintatulos.vastaanottotila == ResultState.EHDOLLISESTI_VASTAANOTTANUT ||
      hakutoiveenValintatulos.vastaanottotila == ResultState.EI_VASTAANOTETTU_MAARA_AIKANA
    }

    private def isVastaanotettavissa(hakutoiveenValintatulos: HakutoiveenValintatulos) = {
      hakutoiveenValintatulos.vastaanotettavuustila == VastaanotettavuusTila.VASTAANOTETTAVISSA_EHDOLLISESTI ||
      hakutoiveenValintatulos.vastaanotettavuustila == VastaanotettavuusTila.VASTAANOTETTAVISSA_SITOVASTI
    }

    private def convertToValintatulos(applicationSystem: ApplicationSystem, application: Application, hakutoiveet: List[Hakutoive]): Option[Valintatulos] = {
      def findKoulutus(oid: String): Koulutus = {
        hakutoiveet.find(_.get("Koulutus-id") == Some(oid)).map{ hakutoive => Koulutus(oid, hakutoive("Koulutus"))}.getOrElse(Koulutus(oid, oid))
      }

      def findOpetuspiste(oid: String): Opetuspiste = {
        hakutoiveet.find(_.get("Opetuspiste-id") == Some(oid)).map{ hakutoive => Opetuspiste(oid, hakutoive("Opetuspiste"))}.getOrElse(Opetuspiste(oid, oid))
      }
      valintatulosService.getValintatulos(application.getOid, applicationSystem.getId).map { valintaTulos =>
        Valintatulos(valintaTulos.hakutoiveet.map { hakutoiveenTulos =>
          HakutoiveenValintatulos(
            findKoulutus(hakutoiveenTulos.hakukohdeOid),
            findOpetuspiste(hakutoiveenTulos.tarjoajaOid),
            HakutoiveenValintatulosTila.withName(hakutoiveenTulos.valintatila),
            convertToResultsState(hakutoiveenTulos),
            VastaanotettavuusTila.withName(hakutoiveenTulos.vastaanotettavuustila),
            hakutoiveenTulos.ilmoittautumistila,
            hakutoiveenTulos.jonosija,
            hakutoiveenTulos.varasijojaTaytetaanAsti.map(_.getTime),
            hakutoiveenTulos.varasijanumero
          )
        })
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
