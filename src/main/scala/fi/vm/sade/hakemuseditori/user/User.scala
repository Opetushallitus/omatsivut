package fi.vm.sade.hakemuseditori.user

trait User {
  def oid: String
  def checkAccessToUserData(personOid: String)
}

case class Oppija(val oid: String) extends User {
  override def toString = "oppija " + oid

  override def checkAccessToUserData(personOid: String): Unit = {
    if (this.oid != personOid) {
      throw new IllegalArgumentException("Person oid mismatch")
    }
  }
}
