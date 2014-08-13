package fi.vm.sade.omatsivut.koulutusinformaatio

case class Koulutus(  id: String,
                      name: String,
                      aoIdentifier: String,
                      educationDegree: String,
                      provider: Option[Opetuspiste],
                      childLONames: List[String],
                      sora: Boolean,
                      teachingLanguages: List[String],
                      athleteEducation: Boolean,
                      kaksoistutkinto: Boolean,
                      vocational: Boolean,
                      educationCodeUri: String,
                      attachmentDeliveryDeadline: Option[Long],
                      attachmentDeliveryAddress: Option[Address])

case class Address(  streetAddress: Option[String],
                     streetAddress2: Option[String],
                     postalCode: Option[String],
                     postOffice: Option[String])
