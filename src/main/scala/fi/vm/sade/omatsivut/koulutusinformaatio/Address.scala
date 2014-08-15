package fi.vm.sade.omatsivut.koulutusinformaatio

case class Address(  streetAddress: Option[String],
                     streetAddress2: Option[String],
                     postalCode: Option[String],
                     postOffice: Option[String])