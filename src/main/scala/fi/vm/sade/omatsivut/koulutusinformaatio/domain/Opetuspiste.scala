package fi.vm.sade.omatsivut.koulutusinformaatio.domain

import fi.vm.sade.omatsivut.domain.Address

case class Opetuspiste(
  id: String,
  name: String,
  applicationOffice: Option[ApplicationOffice]
)

case class ApplicationOffice(postalAddress: Option[Address])
