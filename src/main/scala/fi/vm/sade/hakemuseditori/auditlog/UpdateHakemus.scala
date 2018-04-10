package fi.vm.sade.hakemuseditori.auditlog

import java.net.InetAddress

import fi.vm.sade.auditlog.{Changes, Target, User}
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.omatsivut.security.AuthenticationInfoParser.getAuthenticationInfo
import javax.servlet.http.HttpServletRequest

case class UpdateHakemus(request: HttpServletRequest, userOid: String, hakemusOid: String, hakuOid: String, originalAnswers: Answers, updatedAnswers: Answers) extends AuditLogUtils with AuditEvent {
  override val operation: OmatSivutOperation = OmatSivutOperation.UPDATE_HAKEMUS
  override val target: Target = new Target.Builder()
    .setField(OmatSivutMessageField.MESSAGE, "Tallennettu päivitetty hakemus haussa")
    .setField(OmatSivutMessageField.HAKU_OID, hakuOid)
    .setField(OmatSivutMessageField.HAKEMUS_OID, hakemusOid)
    .build()
  override val changes: Changes = {
    val builder = new Changes.Builder()
    getAnswerDiff.foreach(triplet => {
      builder.updated(triplet.key, triplet.oldValue, triplet.newValue)
    })
    builder.build()
  }

  override def user: User = {
    val authInfo = getAuthenticationInfo(request)
    val shib = authInfo.shibbolethCookie
    new User(getOid(authInfo.personOid.get).orNull, getAddress(request), shib.map(_.toString).getOrElse("(no shibboleth cookie)"), getUserAgent(request))
  }

  /**
    * Gets a list of triplets that contain (key, original value, new value)
    * @return an #Iterable[#DiffTriplet]
    */
  private[hakemuseditori] def getAnswerDiff: Iterable[DiffTriplet] = {
    //this thing assumes that the Answers type contains only ONE nested map inside the outer map
    val diff: Iterable[DiffTriplet] = updatedAnswers.flatMap(keyVal => {
      val key1: String = keyVal._1
      val updated: Map[String, String] = keyVal._2

      val original: Map[String, String] = originalAnswers.getOrElse(key1, Map.empty)

      val diff = (original.toSet diff updated.toSet).toMap[String, String]

      val diffTriplets: Iterable[DiffTriplet] = diff.map(keyVal => {
        val key2 = keyVal._1
        DiffTriplet(s"$key1.$key2", original.getOrElse(key2, ""), updated.getOrElse(key2, ""))
      })
      diffTriplets

      //remove ones where both are empty or equal
    }).filter(triple => (!triple.oldValue.isEmpty || !triple.newValue.isEmpty) && !triple.newValue.equals(triple.oldValue))
    diff
  }

  private[hakemuseditori] case class DiffTriplet(key: String, oldValue: String, newValue: String)
}
