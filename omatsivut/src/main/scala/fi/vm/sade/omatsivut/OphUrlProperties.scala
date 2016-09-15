package fi.vm.sade.omatsivut

import fi.vm.sade.properties.OphProperties
import java.nio.file.Paths

object OphUrlProperties extends OphProperties("/omatsivut-oph.properties") {
    addOptionalFiles(Paths.get(sys.props.get("user.home").get, "/oph-configuration/common.properties").toString)
}