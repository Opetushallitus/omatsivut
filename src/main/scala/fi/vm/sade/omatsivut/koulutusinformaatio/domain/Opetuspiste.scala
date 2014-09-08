package fi.vm.sade.omatsivut.koulutusinformaatio.domain

import fi.vm.sade.omatsivut.domain.Address

case class Opetuspiste(
  id: String,
  name: String,
  applicationOffice: Option[ApplicationOffice]
)

case class ApplicationOffice(name: Option[String], postalAddress: Option[Address])
