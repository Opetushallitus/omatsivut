package fi.vm.sade.omatsivut

import fi.vm.sade.properties.OphProperties
import java.nio.file.Paths

import fi.vm.sade.omatsivut.config.AppConfig

class OphUrlProperties extends OphProperties("/omatsivut-oph.properties") {
  def this(isItProfile: Boolean) {
    this()
    addOptionalFiles(Paths.get(sys.props.get("user.home").get, "/oph-configuration/common.properties").toString)
    if (isItProfile) {
      addOverride("url-oppija", "http://localhost:"+AppConfig.embeddedJettyPortChooser.chosenPort.toString)
      addOverride("url-virkailija", "http://localhost:"+AppConfig.embeddedJettyPortChooser.chosenPort.toString)
    }
  }
}