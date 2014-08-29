package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.valintatulokset.ValintatulosService

object ValintatulosTester extends scala.App with JsonFormats {
  import org.json4s.native.Serialization.write

  implicit val appConfig = new AppConfig.LocalTestingWithTemplatedVars("../deploy/vars/ophitest_vars.yml")
   println(write(ValintatulosService.apply.getValintatulos("asdf", "qwer")))
}