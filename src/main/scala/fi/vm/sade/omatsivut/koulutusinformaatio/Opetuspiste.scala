package fi.vm.sade.omatsivut.koulutusinformaatio

case class Opetuspiste(
  id: String,
  name: String,
  applicationOffice: Option[ApplicationOffice]
)

case class ApplicationOffice(postalAddress: Option[Address])
