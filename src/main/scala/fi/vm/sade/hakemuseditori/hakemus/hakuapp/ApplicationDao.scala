package fi.vm.sade.hakemuseditori.hakemus.hakuapp

import fi.vm.sade.hakemuseditori.hakemus.hakuapp.domain.Application

class ApplicationDao {

  def findByPersonOid(personOid: String): List[Application] = {
    // TODO implement
    List(new Application().setState(Application.State.INCOMPLETE))
  }

  def findByOid(oid: String): Option[Application] = {
    // TODO implement
    Some(new Application())
  }

}
