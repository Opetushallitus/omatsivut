package fi.vm.sade.omatsivut.koodisto

trait KoodistoService {
  type Translations = Map[String, String]
  def postOffices: Map[String, Translations]
}