package fi.vm.sade.hakemuseditori.hakemus

import java.util.Date
import fi.vm.sade.hakemuseditori.hakemus.ImmutableLegacyApplicationWrapper.LegacyApplicationAnswers
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.{Application}
import fi.vm.sade.omatsivut.util.Logging

import scala.collection.JavaConversions._

/**
 * A thin immutable wrapper for the fi.vm.sade.haku.oppija.hakemus.domain.Application class
 */
object ImmutableLegacyApplicationWrapper extends Logging {
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
      Option(application.getReceived),
      Option(application.getUpdated),
      complete,
      Application.State.SUBMITTED.toString // TODO fix voiko olla kovakoodattu vai tarvitaanko päättely
    )
  }

}

case class ImmutableLegacyApplicationWrapper(
                                              oid: String,
                                              hakuOid: String,
                                              personOid: String,
                                              answers: LegacyApplicationAnswers,
                                              received: Option[Date],
                                              updated: Option[Date],
                                              complete: Boolean,
                                              state: String) {
  def phaseAnswers(phase: String): Map[String, String] = answers.getOrElse(phase, Map.empty)
  lazy val flatAnswers = FlatAnswers.flatten(answers)

  def henkilotunnus: Option[String] = answers.get("henkilotiedot").flatMap(_.get("Henkilotunnus"))
  def sähköposti: Option[String] = answers.get("henkilotiedot").flatMap(_.get("Sähköposti"))
  def etunimet: Option[String] = answers.get("henkilotiedot").flatMap(_.get("Etunimet"))
  def sukunimi: Option[String] = answers.get("henkilotiedot").flatMap(_.get("Sukunimi"))
}
