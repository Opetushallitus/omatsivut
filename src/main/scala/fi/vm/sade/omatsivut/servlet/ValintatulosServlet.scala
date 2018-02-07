package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.HakemusEditoriUserContext
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.user.Oppija
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosService
import fi.vm.sade.hakemuseditori.valintatulokset.domain.Ilmoittautuminen
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.security.AuthenticationRequiringServlet
import org.scalatra.json.JacksonJsonSupport

trait ValintatulosServletContainer {

  val valintatulosService: ValintatulosService

  class ValintatulosServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with
                                                              AuthenticationRequiringServlet with HakemusEditoriUserContext {
    override def user() = Oppija(personOid())

    before() {
      contentType = formats("json")
    }

    post("/") {
      val body = parsedBody.extract[Ilmoittautuminen]
      body.muokkaaja = user().oid
      valintatulosService.ilmoittaudu(params("hakuOid"), params("hakemusOid"), body)
    }
  }
}
