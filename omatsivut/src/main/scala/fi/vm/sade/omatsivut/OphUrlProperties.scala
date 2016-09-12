package fi.vm.sade.omatsivut

import fi.vm.sade.properties.OphProperties
import java.nio.file.Paths

import com.typesafe.config.Config
import fi.vm.sade.omatsivut.config.AppConfig

class OphUrlProperties extends OphProperties("/omatsivut-oph.properties") {
  def this(config: Config) {
    this()
    addOptionalFiles(Paths.get(sys.props.getOrElse("user.home", ""), "/oph-configuration/common.properties").toString)
    addOverride("url-oppija", config.getValue("host.haku").unwrapped().asInstanceOf[String])
    addOverride("url-virkailija", "http://" + config.getValue("host.virkailija").unwrapped().asInstanceOf[String] + ":"
      + AppConfig.embeddedJettyPortChooser.chosenPort.toString)
  }
}