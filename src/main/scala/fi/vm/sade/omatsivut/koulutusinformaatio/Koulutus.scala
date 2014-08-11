package fi.vm.sade.omatsivut.koulutusinformaatio

import org.joda.time.DateTime

case class Koulutus(  id: String,
                      name: String,
                      aoIdentifier: String,
                      educationDegree: String,
                      childLONames: List[String],
                      sora: Boolean,
                      teachingLanguages: List[String],
                      athleteEducation: Boolean,
                      kaksoistutkinto: Boolean,
                      vocational: Boolean,
                      educationCodeUri: String,
                      attachmentDeliveryDeadline: Long,
                      attachmentDeliveryAddress: Address)

case class Address(  streetAddress: String,
                     streetAddress2: String,
                     postalCode: String,
                     postOffice: String)
