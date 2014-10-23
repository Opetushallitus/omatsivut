package fi.vm.sade.omatsivut.koodisto

trait KoodistoService {
  type Translations = Map[String, String]
  def postOffice(postalCode: String): Option[Translations]
}