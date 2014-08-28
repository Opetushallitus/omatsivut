package fi.vm.sade.omatsivut.hakemus

import java.util
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain._
import scala.collection.JavaConversions._
import scala.util.Try
import fi.vm.sade.haku.oppija.hakemus.domain.util.ApplicationUtil
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem

object HakemusConverter {
  val educationPhaseKey = OppijaConstants.PHASE_EDUCATION
  val baseEducationKey = OppijaConstants.ELEMENT_ID_BASE_EDUCATION
  val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS

  def convertToHakemus(applicationSystem: ApplicationSystem, haku: Haku)(application: Application) = {
    val koulutusTaustaAnswers: util.Map[String, String] = application.getAnswers.get(educationPhaseKey)
    Hakemus(
      application.getOid,
      application.getReceived.getTime,
      application.getUpdated.getTime,
      tila(applicationSystem, application),
      convertHakuToiveet(application),
      haku,
      EducationBackground(koulutusTaustaAnswers.get(baseEducationKey), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false)),
      application.clone().getAnswers.toMap.mapValues { phaseAnswers => phaseAnswers.toMap },
      requiresAdditionalInfo(applicationSystem, application)
    )
  }

  def tila(applicationSystem: ApplicationSystem, application: Application): HakemuksenTila = {
    if (isPostProcessing(application)) {
      PostProcessing()
    } else {
      application.getState.toString match {
        case "ACTIVE" => Active(valintaTulos = getValintaTulos(application, applicationSystem))
        case "PASSIVE" => Passive()
        case "INCOMPLETE" => Incomplete()
        case "SUBMITTED" => Submitted()
        case x => {
          throw new RuntimeException("Unexpected state for application " + application.getOid + ": " + x)
        }
      }
    }
  }

  def getValintaTulos(application: Application, system: ApplicationSystem) = {
    None
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
