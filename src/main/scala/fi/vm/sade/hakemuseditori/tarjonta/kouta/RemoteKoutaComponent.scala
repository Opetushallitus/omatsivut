package fi.vm.sade.hakemuseditori.tarjonta.kouta

import java.util.concurrent.TimeUnit

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaService
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde, KoutaHaku}
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s.Method.GET
import org.http4s.client.blaze
import org.http4s.{Request, Response, Uri}
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import scalaz.concurrent.Task

import scala.concurrent.duration.Duration

trait RemoteKoutaComponent {
  this: OhjausparametritComponent =>

  class RemoteKoutaService(config: AppConfig) extends TarjontaService with Logging {
    private val blazeHttpClient = blaze.defaultClient
    private val casClient = new CasClient(config.settings.securitySettings.casUrl, blazeHttpClient)
    private val casParams = CasParams(
      OphUrlProperties.url("kouta-internal.service"),
      "auth/login",
      config.settings.securitySettings.casUsername,
      config.settings.securitySettings.casPassword
    )
    private val httpClient = CasAuthenticatingClient(
      casClient,
      casParams,
      blazeHttpClient,
      AppConfig.callerId,
      "session"
    )

    implicit private val formats = DefaultFormats

    override def haku(oid: String, lang: Language.Language) : Option[Haku] = {
      Uri.fromString(OphUrlProperties.url("kouta-internal.haku", oid))
        .fold(Task.fail, uri => {
          logger.info(s"Get haku $oid from Kouta: uri $uri")
          httpClient.fetch(Request(method = GET, uri = uri)) { r => handleHakuResponse(r, lang) }
        })
        .unsafePerformSyncAttemptFor(Duration(10, TimeUnit.SECONDS))
        .fold(throw _, x => x)
    }

    private def handleHakuResponse(response: Response, lang: Language): Task[Option[Haku]] = {
      response match {
        case r if r.status.code == 200 =>
          r.as[String]
            .map(s => JsonMethods.parse(s).extract[KoutaHaku])
            .map({ koutaHaku =>
              Some(Haku.fromKoutaHaku(koutaHaku, lang))
            })
        case r if r.status.code == 404 =>
          Task.now(None)
        case r =>
          Task.fail(new RuntimeException(s"Failed to get haku: ${r.toString()}"))
      }
    }

    override def hakukohde(oid: String): Option[Hakukohde] = {
      Uri.fromString(OphUrlProperties.url("kouta-internal.hakukohde", oid))
        .fold(Task.fail, uri => {
          logger.info(s"Get hakukohde $oid from Kouta: uri $uri")
          httpClient.fetch(Request(method = GET, uri = uri)) { r => handleHakukohdeResponse(r) }
        })
        .unsafePerformSyncAttemptFor(Duration(10, TimeUnit.SECONDS))
        .fold(throw _, x => x)
    }

    private def handleHakukohdeResponse(response: Response): Task[Option[Hakukohde]] = {
      response match {
        case r if r.status.code == 200 =>
          r.as[String]
            .map( s => JsonMethods.parse(s).extract[KoutaHakukohde] )
            .map( koutaHakukohde => Some(KoutaHakukohde.toHakukohde(koutaHakukohde)) )
        case r if r.status.code == 404 =>
          Task.now(None)
        case r =>
          Task.fail(new RuntimeException(s"Failed to get hakukohde: ${r.toString()}"))
      }
    }
  }

}
