package fi.vm.sade.omatsivut

import fi.vm.sade.properties.OphProperties
import java.nio.file.Paths

import fi.vm.sade.omatsivut.config.AppConfig

class OphUrlProperties extends OphProperties("/omatsivut-oph.properties") {
  def this(urlVirkailija: String, urlOppija: String) {
    this()
    addOptionalFiles(Paths.get(sys.props.get("user.home").get, "/oph-configuration/common.properties").toString)
    addOverride("url-oppija", urlOppija)
    addOverride("url-virkailija", urlVirkailija)
  }
}