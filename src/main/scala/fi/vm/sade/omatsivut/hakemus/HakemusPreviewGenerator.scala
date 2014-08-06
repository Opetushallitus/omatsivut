package fi.vm.sade.omatsivut.hakemus

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.Logging

case class HakemusPreviewGenerator(implicit val appConfig: AppConfig) extends Logging {
  def generatePreview(personOid: String, applicationOid: String): Option[String] = {
    Some("<div>preview</div>")
  }
}
