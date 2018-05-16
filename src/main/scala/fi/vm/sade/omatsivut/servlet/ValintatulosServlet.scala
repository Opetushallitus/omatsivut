package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.HakemusEditoriUserContext
import fi.vm.sade.hakemuseditori.auditlog.{Audit, SaveIlmoittautuminen}
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
      val ilmoittautuminen = parsedBody.extract[Ilmoittautuminen]
      val hakuOid = params("hakuOid")
      val hakemusOid = params("hakemusOid")

      ilmoittautuminen.muokkaaja = user().oid
      val bool = valintatulosService.ilmoittaudu(params("hakuOid"), params("hakemusOid"), ilmoittautuminen)
      Audit.oppija.log(SaveIlmoittautuminen(request, hakuOid, hakemusOid, ilmoittautuminen, bool))
    }
  }
}
