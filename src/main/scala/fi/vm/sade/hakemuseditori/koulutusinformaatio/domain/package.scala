package fi.vm.sade.hakemuseditori.koulutusinformaatio.domain

import fi.vm.sade.hakemuseditori.domain.Address

case class Koulutus(  id: String,
                      name: String,
                      aoIdentifier: String,
                      educationDegree: String,
                      provider: Option[Opetuspiste],
                      sora: Boolean,
                      soraDescription: Option[String],
                      teachingLanguages: List[String],
                      athleteEducation: Boolean,
                      kysytaanHarkinnanvaraiset: Boolean,
                      kaksoistutkinto: Boolean,
                      vocational: Boolean,
                      educationCodeUri: String,
                      attachments: List[Attachment],
                      attachmentDeliveryDeadline: Option[Long],
                      attachmentDeliveryAddress: Option[Address],
                      organizationGroups: List[OrganizationGroup],
                      applicationStartDate: Option[Long],
                      applicationEndDate: Option[Long],
                      requiredBaseEducations: Option[List[String]]
                     )

case class Attachment(  dueDate: Option[Long],
                        `type`: Option[String],
                        descreption: Option[String],
                        address: Option[Address],
                        emailAddr: Option[String]
                       )

case class OrganizationGroup(  oid: String,
                               usageGroups: List[String],
                               groupTypes: List[String]
                              )

case class Opetuspiste(
                        id: String,
                        name: String,
                        applicationOffice: Option[ApplicationOffice]
                        )

case class ApplicationOffice(name: Option[String], postalAddress: Option[Address])

