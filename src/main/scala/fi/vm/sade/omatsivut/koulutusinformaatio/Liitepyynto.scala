package fi.vm.sade.omatsivut.koulutusinformaatio

case class Liitepyynto (
                    oid: String,
                    name: Option[String] = None,
                    address: Option[Address] = None,
                    deadline: Option[Long] = None
                  )
