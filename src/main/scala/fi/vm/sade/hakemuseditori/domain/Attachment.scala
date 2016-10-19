package fi.vm.sade.hakemuseditori.domain


case class Attachment (
                    name: Option[String],
                    heading: Option[String],
                    description: Option[String],
                    recipientName: Option[String],
                    address: Option[Address],
                    deadline: Option[Long],
                    emailAddress: Option[String]
                  )
