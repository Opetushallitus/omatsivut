package fi.vm.sade.omatsivut.koulutusinformaatio

case class Liitepyynto (
                    oid: String,
                    name: Option[String],
                    address: Option[Address],
                    deadline: Option[Long]
                  )
