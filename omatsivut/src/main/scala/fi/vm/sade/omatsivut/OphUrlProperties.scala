package fi.vm.sade.omatsivut

import java.nio.file.Paths

import fi.vm.sade.properties.OphProperties

object OphUrlProperties extends OphProperties("/omatsivut-oph.properties") {
  addOptionalFiles(Paths.get(sys.props.getOrElse("user.home", ""), "/oph-configuration/common.properties").toString)
}
