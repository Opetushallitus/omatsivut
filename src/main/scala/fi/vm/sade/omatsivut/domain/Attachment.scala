package fi.vm.sade.omatsivut.domain


case class Attachment (
                    heading: Option[String],
                    description: Option[String],
                    recipientName: Option[String],
                    address: Option[Address],
                    deadline: Option[Long]
                  )
