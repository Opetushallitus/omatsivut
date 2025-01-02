package fi.vm.sade.hakemuseditori.tarjonta.kouta

import java.util.concurrent.TimeUnit
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.ohjausparametrit.domain.HaunAikataulu
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaService
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde}
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.util.Logging
import org.asynchttpclient.{RequestBuilder}

import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s.DefaultFormats
import org.json4s.jackson.JsonMethods

import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.{Await}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

trait RemoteKoutaComponent {
  this: OhjausparametritComponent =>

  class RemoteKoutaService(config: AppConfig) extends TarjontaService with Logging {
    private val casConfig: CasConfig = new CasConfig.CasConfigBuilder(
      config.settings.securitySettings.casVirkailijaUsername,
      config.settings.securitySettings.casVirkailijaPassword,
      OphUrlProperties.url("cas.virkailija.url"),
      OphUrlProperties.url("kouta-internal.service"),
      AppConfig.callerId,
      AppConfig.callerId,
      "/auth/login")
      .setJsessionName("session").build

    private val casClient: CasClient = CasClientBuilder.build(casConfig)

    implicit private val formats = DefaultFormats

    override def haku(oid: String, lang: Language.Language) : Option[Haku] = {
      val haunAikataulu = ohjausparametritService.haunAikataulu(oid)
      fetchKoutaHaku(oid) match {
        case Right(o) => o.flatMap { koutaHaku: KoutaHaku => toHaku(koutaHaku, lang, haunAikataulu) }
        case Left(e) => throw new RuntimeException(e)
      }
    }

    private def fetchKoutaHaku(oid: String): Either[Throwable,Option[KoutaHaku]] = {
      val request = new RequestBuilder()
        .setMethod("GET")
        .setUrl(OphUrlProperties.url("kouta-internal.haku", oid))
        .build()
      logger.info(s"Get haku $oid from Kouta: uri ${request.getUri}")
      val result = toScala(casClient.execute(request)).map {
          case r if r.getStatusCode == 200 =>
            Right(Some(JsonMethods.parse(r.getResponseBodyAsStream()).extract[KoutaHaku]))
          case r if r.getStatusCode == 404 =>
            Right(None)
          case r =>
            Left(new RuntimeException(s"Failed to get haku: ${r.toString()}"))
      } // TODO retry?
      try {
        Await.result(result, Duration(10, TimeUnit.SECONDS))
      } catch {
        case e: Throwable => Left(e)
      }
    }

    private def toHaku(
      koutaHaku: KoutaHaku,
      lang: Language.Language,
      haunAikataulu: Option[HaunAikataulu]) : Option[Haku] = {

      KoutaHaku.toHaku(koutaHaku, lang, haunAikataulu, config, ohjausparametritService) match {
        case Success(haku) => Some(haku)
        case Failure(exception) =>
          throw new RuntimeException(s"Failed to convert KoutaHaku: oid ${koutaHaku.oid}", exception)
      }
    }

    override def hakukohde(oid: String, lang: Language.Language): Option[Hakukohde] = {
      fetchKoutaHakukohde(oid) match {
        case Right(o) => o.flatMap { koutaHakukohde => toHakukohde(koutaHakukohde, lang) }
        case Left(e) => throw new RuntimeException(e)
      }
    }

    private def fetchKoutaHakukohde(oid: String) = {
      val request = new RequestBuilder()
        .setMethod("GET")
        .setUrl(OphUrlProperties.url("kouta-internal.hakukohde", oid))
        .build()
      val result = toScala(casClient.execute(request)).map {
        case r if r.getStatusCode == 200 =>
          Right(Some(JsonMethods.parse(r.getResponseBodyAsStream()).extract[KoutaHakukohde]))
        case r if r.getStatusCode == 404 =>
          Right(None)
        case r =>
          Left(new RuntimeException(s"Failed to get hakukohde: ${r.toString()}"))
      } // TODO retry?
      try {
        Await.result(result, Duration(10, TimeUnit.SECONDS))
      } catch {
        case e: Throwable => Left(e)
      }
    }

    private def toHakukohde(koutaHakukohde: KoutaHakukohde, lang: Language.Language): Option[Hakukohde] = {
      KoutaHakukohde.toHakukohde(koutaHakukohde, lang) match {
        case Success(hakukohde) => Some(hakukohde)
        case Failure(exception) => throw new RuntimeException(s"Failed to convert KoutaHakukohde: oid ${koutaHakukohde.oid}", exception)
      }
    }
  }

}
