package fi.vm.sade.omatsivut.domain


case class Attachment (
                    heading: String,
                    description: String,
                    providerName: Option[String] = None,
                    recipientName: Option[String] = None,
                    address: Option[Address] = None,
                    deadline: Option[Long] = None
                  )
