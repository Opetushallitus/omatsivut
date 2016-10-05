package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.config.{ComponentRegistry, AppConfig}
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosService

object ValintatulosTester extends scala.App with JsonFormats {
  import org.json4s.native.Serialization.write

  val appConfig = new AppConfig.LocalTestingWithTemplatedVars("../deploy/vars/ophitest_vars.yml")
  val componentRegistry = new ComponentRegistry(appConfig)

  println(write(componentRegistry.valintatulosService.getValintatulos("asdf", "qwer")))
}