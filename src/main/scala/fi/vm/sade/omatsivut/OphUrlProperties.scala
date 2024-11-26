package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.util.OphProperties

import java.nio.file.Paths


object OphUrlProperties extends OphProperties("/omatsivut-oph.properties") {
//  debugMode()
  addOptionalFiles(Paths.get(sys.props.getOrElse("user.home", ""), "/oph-configuration/common.properties").toString)
}
