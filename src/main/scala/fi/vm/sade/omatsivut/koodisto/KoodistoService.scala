package fi.vm.sade.omatsivut.koodisto

import fi.vm.sade.omatsivut.domain.Language.Language

trait KoodistoService {
  def postOffices(implicit lang: Language): Map[String, String]
}