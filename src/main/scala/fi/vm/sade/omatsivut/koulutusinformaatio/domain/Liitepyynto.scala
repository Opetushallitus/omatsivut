package fi.vm.sade.omatsivut.koulutusinformaatio.domain

import fi.vm.sade.omatsivut.domain.Address

case class Liitepyynto (
                    oid: String,
                    name: Option[String] = None,
                    address: Option[Address] = None,
                    deadline: Option[Long] = None
                  )
