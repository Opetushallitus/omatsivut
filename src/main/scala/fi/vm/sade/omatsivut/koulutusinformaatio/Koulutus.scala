package fi.vm.sade.omatsivut.koulutusinformaatio

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
                      educationCodeUri: String)
