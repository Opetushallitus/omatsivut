package fi.vm.sade.hakemuseditori.hakemus.hakuapp

class ApplicationDao {

  def findByPersonOid(personOid: String): List[Application] = {
    // TODO implement
    List(new Application())
  }

  def findByOid(oid: String): Option[Application] = {
    // TODO implement
    Some(new Application())
  }

}
