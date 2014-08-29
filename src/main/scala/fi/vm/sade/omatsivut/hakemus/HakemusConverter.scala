package fi.vm.sade.omatsivut.hakemus

import java.util
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._
import fi.vm.sade.omatsivut.valintatulokset.ValintatulosService
import org.json4s.jackson.JsonMethods._
import scala.collection.JavaConversions._
import scala.util.Try
import fi.vm.sade.haku.oppija.hakemus.domain.util.ApplicationUtil
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem

object HakemusConverter {
  val educationPhaseKey = OppijaConstants.PHASE_EDUCATION
  val baseEducationKey = OppijaConstants.ELEMENT_ID_BASE_EDUCATION
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def convertToHakemus(applicationSystem: ApplicationSystem, haku: Haku, application: Application)(implicit appConfig: AppConfig) = {
    val koulutusTaustaAnswers: util.Map[String, String] = application.getAnswers.get(educationPhaseKey)
    Hakemus(
      application.getOid,
      application.getReceived.getTime,
      application.getUpdated.getTime,
      tila(applicationSystem, haku, application),
      convertHakuToiveet(application),
      haku,
      EducationBackground(koulutusTaustaAnswers.get(baseEducationKey), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false)),
      application.clone().getAnswers.toMap.mapValues { phaseAnswers => phaseAnswers.toMap },
      requiresAdditionalInfo(applicationSystem, application)
    )
  }

  def tila(applicationSystem: ApplicationSystem, haku: Haku, application: Application)(implicit appConfig: AppConfig): HakemuksenTila = {
    if (isPostProcessing(application)) {
      PostProcessing()
    } else {
      application.getState.toString match {
        case "ACTIVE" => if (!haku.applicationPeriods.head.active) {
          HakuPaattynyt(valintaTulos = valintaTulos(applicationSystem, application))
        } else {
          Active()
        }
        case "PASSIVE" => Passive()
        case "INCOMPLETE" => Incomplete()
        case "SUBMITTED" => Submitted()
        case x => {
          throw new RuntimeException("Unexpected state for application " + application.getOid + ": " + x)
        }
      }
    }
  }

  def valintaTulos(applicationSystem: ApplicationSystem, application: Application)(implicit appConfig: AppConfig) = {
    val hakutoiveet = convertHakuToiveet(application)

    def findKoulutus(oid: String): Koulutus = {
      hakutoiveet.find(_.get("Koulutus-id") == Some(oid)).map{ hakutoive => Koulutus(oid, hakutoive("Koulutus"))}.getOrElse(Koulutus(oid, oid))
    }

    def findOpetuspiste(oid: String): Opetuspiste = {
      hakutoiveet.find(_.get("Opetuspiste-id") == Some(oid)).map{ hakutoive => Opetuspiste(oid, hakutoive("Opetuspiste"))}.getOrElse(Opetuspiste(oid, oid))
    }
    ValintatulosService.apply.getValintatulos(application.getOid, applicationSystem.getId).map { valintaTulos =>
      Valintatulos(valintaTulos.hakutoiveet.map { hakutoiveenTulos =>
        HakutoiveenValintatulos(
          findKoulutus(hakutoiveenTulos.hakukohdeOid),
          findOpetuspiste(hakutoiveenTulos.tarjoajaOid),
          hakutoiveenTulos.tila,
          hakutoiveenTulos.vastaanottotieto,
          hakutoiveenTulos.ilmoittautumisTila,
          hakutoiveenTulos.jonosija,
          hakutoiveenTulos.varasijaNumero
        )
      })
    }
  }

  private def requiresAdditionalInfo(applicationSystem: ApplicationSystem, application: Application): Boolean = {
    !ApplicationUtil.getDiscretionaryAttachmentAOIds(application).isEmpty() ||
    !ApplicationUtil.getHigherEdAttachmentAOIds(application).isEmpty() ||
    !(for(addInfo <- applicationSystem.getAdditionalInformationElements())
      yield ElementWrapper.wrapFiltered(addInfo, flattenAnswers(application.getAnswers().toMap.mapValues(_.toMap)))
    ).filterNot(_.children.isEmpty).isEmpty
  }

  private def isPostProcessing(application: Application): Boolean = {
    val state = application.getRedoPostProcess
    !(state == Application.PostProcessingState.DONE || state == null)
  }

  private def convertHakuToiveet(application: Application): List[Hakutoive] = {
    HakutoiveetConverter.convertFromAnswers(application.getAnswers.toMap.mapValues(_.toMap))
  }

  type FlatAnswers = Map[String, String]

  def flattenAnswers(answers: Map[String, Map[String, String]]): Map[String, String] = {
    answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
  }
}
