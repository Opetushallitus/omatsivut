package fi.vm.sade.hakemuseditori.auditlog

import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.user.{Oppija, User}

import scala.collection.immutable

//TODO improve the diff handling, possibly by improving the AuditEvent (which this extends) to allow for more generic log text fomation
case class UpdateHakemus(user: User, hakemusOid: String, hakuOid: String, originalAnswers: Answers, updatedAnswers: Answers, target: String = "Hakemus") extends AuditEvent {
  override def isUserOppija = user match {
    case u: Oppija => true
    case _ => false
  }
  override def toLogMessage = Map(
    "message" -> "Tallennettu päivitetty hakemus haussa",
    "hakuOid" -> hakuOid,
    "hakemusOid" -> hakemusOid,
    "id" -> user.oid)

  /**
    * Gets a list of triplets that contain (key, original value, new value)
    * @return
    */
  private[hakemuseditori] def getAnswerDiff: Iterable[(String, String, String)] = {


    //this thing assumes that the Answers type contains only ONE nested map inside the outer map
    val diff: immutable.Iterable[(String, String, String)] = updatedAnswers.flatMap(keyVal => {
      val key1: String = keyVal._1
      val updated: Map[String, String] = keyVal._2

      val original: Map[String, String] = originalAnswers.getOrElse(key1, Map.empty)

      val diff = (original.toSet diff updated.toSet).toMap[String, String]

      val diffTriplets: immutable.Iterable[(String, String, String)] = diff.map(keyVal => {
        val key2 = keyVal._1
        (s"$key1.$key2", original.getOrElse(key2, ""), updated.getOrElse(key2, ""))
      })
      diffTriplets
    }).filter(triple => !triple._2.isEmpty || !triple._3.isEmpty)


    diff
  }
}
