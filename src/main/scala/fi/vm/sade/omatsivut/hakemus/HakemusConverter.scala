package fi.vm.sade.omatsivut.hakemus

import java.util
import fi.vm.sade.haku.oppija.hakemus.domain.Application
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.omatsivut.domain.Hakemus._
import fi.vm.sade.omatsivut.domain.{EducationBackground, Hakemus, Haku}
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
      application.getState.toString,
      isPostProcessing(application),
      convertHakuToiveet(application),
      haku,
      EducationBackground(koulutusTaustaAnswers.get(baseEducationKey), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false)),
      application.getAnswers.toMap.mapValues { phaseAnswers => phaseAnswers.toMap },
      requiresAdditionalInfo(applicationSystem, application)
    )
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
    val answers: util.Map[String, util.Map[String, String]] = application.getAnswers
    val hakuToiveetData: Map[String, String] = answers.get(preferencePhaseKey).toMap
    HakutoiveetConverter.convertFromAnswers(hakuToiveetData)
  }

  type FlatAnswers = Map[String, String]

  def flattenAnswers(answers: Map[String, Map[String, String]]): Map[String, String] = {
    answers.values.foldLeft(Map.empty.asInstanceOf[Map[String, String]]) { (a,b) => a ++ b }
  }
}
