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
                      attachmentDeliveryAddress: Option[Address],
                      organizationGroups: List[OrganizationGroup]
)

case class OrganizationGroup(  oid: String,
                               usageGroups: List[String],
                               groupTypes: List[String]
)
