package fi.vm.sade.omatsivut.hakemus

import java.util.Date

import fi.vm.sade.haku.oppija.hakemus.domain.util.AttachmentUtil
import fi.vm.sade.haku.oppija.hakemus.domain.{Application, ApplicationAttachment}
import fi.vm.sade.omatsivut.hakemus.ImmutableLegacyApplicationWrapper.LegacyApplicationAnswers
import scala.collection.JavaConversions._

/**
 * A thin immutable wrapper for the fi.vm.sade.haku.oppija.hakemus.domain.Application class
 */
object ImmutableLegacyApplicationWrapper {
  /**
   *  Answer map from legacy Application objects, containing answers and hakutoiveet
   */
  type LegacyApplicationAnswers = Map[String, Map[String, String]]

  def wrap(application: Application) = {
    val answers: LegacyApplicationAnswers = {
      application.getAnswers.toMap.mapValues { phaseAnswers => phaseAnswers.toMap }
    }
    val oid: String = application.getOid
    val complete: Boolean = {
      application.getState != Application.State.INCOMPLETE
    }
    ImmutableLegacyApplicationWrapper(
      oid,
      application.getApplicationSystemId,
      application.getPersonOid,
      answers,
      AttachmentUtil.resolveAttachments(application).toList,
      application.getReceived,
      Option(application.getUpdated),
      complete,
      application.getState.toString,
      isPostProcessing(application))
  }

  private def isPostProcessing(application: Application): Boolean = {
    val state = application.getRedoPostProcess
    !(state == Application.PostProcessingState.DONE || state == null)
  }


}

case class ImmutableLegacyApplicationWrapper(
                                              oid: String,
                                              hakuOid: String,
                                              personOid: String,
                                              answers: LegacyApplicationAnswers,
                                              attachments: List[ApplicationAttachment],
                                              received: Date,
                                              updated: Option[Date],
                                              complete: Boolean,
                                              state: String,
                                              isPostProcessing: Boolean) {
  def phaseAnswers(phase: String): Map[String, String] = answers.getOrElse(phase, Map.empty)
  lazy val flatAnswers = FlatAnswers.flatten(answers)
}
