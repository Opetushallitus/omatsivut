package fi.vm.sade.hakemuseditori.domain

case class Address(  streetAddress: Option[String],
                     streetAddress2: Option[String],
                     postalCode: Option[String],
                     postOffice: Option[String])