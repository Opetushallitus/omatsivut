package fi.vm.sade.hakemuseditori.tarjonta.kouta

import java.util.concurrent.TimeUnit

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaService
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde}
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import fi.vm.sade.utils.slf4j.Logging
import org.http4s.{Request, Response, Uri}
import org.http4s.Method.GET
import org.http4s.client.blaze
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods
import scalaz.concurrent.Task

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

trait RemoteKoutaComponent {
  this: OhjausparametritComponent =>

  class RemoteKoutaService(config: AppConfig, casVirkailijaClient: CasClient) extends TarjontaService with Logging {
    private val casParams = CasParams(
      OphUrlProperties.url("kouta-internal.service"),
      "auth/login",
      config.settings.securitySettings.casVirkailijaUsername,
      config.settings.securitySettings.casVirkailijaPassword
    )
    private val httpClient = CasAuthenticatingClient(
      casVirkailijaClient,
      casParams,
      blaze.defaultClient,
      AppConfig.callerId,
      "session"
    )

    implicit private val formats = DefaultFormats

    override def haku(oid: String, lang: Language.Language) : Option[Haku] = {
      val haunAikataulu = ohjausparametritService.haunAikataulu(oid)
      fetchKoutaHaku(oid)
        .flatMap { koutaHaku: KoutaHaku => toHaku(koutaHaku, lang, haunAikataulu) }
    }

    private def fetchKoutaHaku(oid: String) = {
      Uri.fromString(OphUrlProperties.url("kouta-internal.haku", oid))
        .fold(Task.fail, uri => {
          logger.info(s"Get haku $oid from Kouta: uri $uri")
          httpClient.fetch(Request(method = GET, uri = uri)) { r => handleHakuResponse(r) }
        })
        .unsafePerformSyncAttemptFor(Duration(10, TimeUnit.SECONDS))
        .fold(throw _, x => x)
    }

    private def handleHakuResponse(response: Response) = {
      response match {
        case r if r.status.code == 200 =>
          r.as[String]
            .map(s => Some(JsonMethods.parse(s).extract[KoutaHaku]))
        case r if r.status.code == 404 =>
          Task.now(None)
        case r =>
          Task.fail(new RuntimeException(s"Failed to get haku: ${r.toString()}"))
      }
    }

    private def toHaku(
      koutaHaku: KoutaHaku,
      lang: Language.Language,
      haunAikataulu: Option[HaunAikataulu]) : Option[Haku] = {

      KoutaHaku.toHaku(koutaHaku, lang, haunAikataulu) match {
        case Success(haku) => Some(haku)
        case Failure(exception) =>
          throw new RuntimeException(s"Failed to convert KoutaHaku: oid ${koutaHaku.oid}", exception)
      }
    }

    override def hakukohde(oid: String): Option[Hakukohde] = {
      fetchKoutaHakukohde(oid)
        .flatMap { koutaHakukohde => toHakukohde(koutaHakukohde) }
    }

    private def fetchKoutaHakukohde(oid: String) = {
      Uri.fromString(OphUrlProperties.url("kouta-internal.hakukohde", oid))
        .fold(Task.fail, uri => {
          logger.info(s"Get hakukohde $oid from Kouta: uri $uri")
          httpClient.fetch(Request(method = GET, uri = uri)) { r => handleHakukohdeResponse(r) }
        })
        .unsafePerformSyncAttemptFor(Duration(10, TimeUnit.SECONDS))
        .fold(throw _, x => x)
    }

    private def handleHakukohdeResponse(response: Response): Task[Option[KoutaHakukohde]] = {
      response match {
        case r if r.status.code == 200 =>
          r.as[String]
            .map( s => Some(JsonMethods.parse(s).extract[KoutaHakukohde]) )
        case r if r.status.code == 404 =>
          Task.now(None)
        case r =>
          Task.fail(new RuntimeException(s"Failed to get hakukohde: ${r.toString()}"))
      }
    }

    private def toHakukohde(koutaHakukohde: KoutaHakukohde): Option[Hakukohde] = {
      KoutaHakukohde.toHakukohde(koutaHakukohde) match {
        case Success(hakukohde) => Some(hakukohde)
        case Failure(exception) => throw new RuntimeException(s"Failed to convert KoutaHakukohde: oid ${koutaHakukohde.oid}", exception)
      }
    }
  }

}
