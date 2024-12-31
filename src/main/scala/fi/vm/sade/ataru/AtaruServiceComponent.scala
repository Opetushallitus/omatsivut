package fi.vm.sade.ataru

import java.time.Instant
import java.util.concurrent.TimeUnit
import fi.vm.sade.hakemuseditori.auditlog.{Audit, ShowHakemus}
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, ValintatulosFetchStrategy}
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, EducationBackground, HakemuksenTila, Hakemus, HakukausiPaattynyt, HakukierrosPaattynyt, Hakutoive}
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde, KohteenHakuaika}
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.{AccessibleHtml, TuloskirjeComponent}
import fi.vm.sade.javautils.nio.cas.{CasClient, CasClientBuilder, CasConfig}
import fi.vm.sade.omatsivut.OmatsivutServer.logger
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import org.asynchttpclient.RequestBuilder

import scala.concurrent.ExecutionContext.Implicits.global
import javax.servlet.http.HttpServletRequest
import org.joda.time.LocalDateTime
import org.json4s.FieldSerializer.{renameFrom, renameTo}
import org.json4s.{DefaultFormats, FieldSerializer}
import org.json4s.jackson.JsonMethods.parse

import scala.compat.java8.FutureConverters.toScala
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

case class AtaruApplication(oid: String,
                            secret: String,
                            email: String,
                            haku: Option[String],
                            hakukohteet: List[String],
                            submitted: String,
                            formName: Option[Map[String, String]])

trait AtaruServiceComponent  {
  this: TarjontaComponent
    with OppijanumerorekisteriComponent
    with ValintatulosServiceComponent
    with TuloskirjeComponent =>

  val ataruService: AtaruService

  trait AtaruServiceCommons extends AtaruService {
    def getApplications(personOid: String): List[AtaruApplication]

    def findApplications(request: HttpServletRequest,
                         personOid: String,
                         valintatulosFetchStrategy: ValintatulosFetchStrategy,
                         language: Language): List[HakemusInfo] = {
      val now = new LocalDateTime().toDate.getTime
      val henkilo = oppijanumerorekisteriService.henkilo(personOid)

      def translate(name: Map[String,String]): Option[String] =
        List(language.toString, "fi", "en", "sv")
          .map(name.get)
          .find(_.isDefined)
          .flatten

      getApplications(personOid)
        .map(a => (
          a,
          a.haku.flatMap(tarjontaService.haku(_, language)),
          getHakukohteet(a.hakukohteet, language),
          a.haku.flatMap(tuloskirjeService.getTuloskirjeInfo(request, _, a.oid, AccessibleHtml))
        ))
        .collect {
          case (a, Some(haku), Some(hakukohteet), tuloskirje) =>
            val valintatulos = Try(if (valintatulosFetchStrategy.ataru(haku, henkilo, a)) {
              valintatulosService.getValintatulos(a.oid, a.haku.get)
            } else {
              None
            })
            val ohjeetUudelleOpiskelijalleMap: Map[String, String] = hakukohteet
              .filter(h => h.ohjeetUudelleOpiskelijalle.isDefined)
              .map(h => h.oid -> h.ohjeetUudelleOpiskelijalle.get)
              .toMap
            val hakutoiveet = hakukohteet.map(toHakutoive)
            val hakemus = Hakemus(
              oid = a.oid,
              personOid = personOid,
              received = Option.apply(Instant.parse(a.submitted).toEpochMilli),
              updated = None,
              state = state(now, Some(haku), hakukohteet, a, valintatulos.getOrElse(None)),
              tuloskirje = tuloskirje,
              ohjeetUudelleOpiskelijalle = ohjeetUudelleOpiskelijalleMap,
              hakutoiveet = hakutoiveet,
              haku = Some(haku),
              educationBackground = EducationBackground("base_education", false),
              answers = Map(),
              postOffice = None,
              email = Some(a.email),
              requiresAdditionalInfo = false,
              hasForm = true,
              requiredPaymentState = None,
              notifications = Map(),
              oppijanumero = henkilo.oppijanumero.getOrElse(personOid),
              formName = translate(a.formName.getOrElse(Map()))
            )
            Audit.oppija.log(ShowHakemus(request, hakemus.personOid, hakemus.oid, haku.oid))
            HakemusInfo(
              hakemus = hakemus,
              errors = List(),
              questions = List(),
              tulosOk = valintatulos.isSuccess,
              paymentInfo = None,
              hakemusSource = "Ataru",
              previewUrl = Some(OphUrlProperties.url("ataru.applications.modify", a.secret))
            )
          case (a, None, _, tuloskirje) =>
            val hakemus = Hakemus(
              oid = a.oid,
              personOid = personOid,
              received = Option.apply(Instant.parse(a.submitted).toEpochMilli),
              updated = None,
              state = state(now, None, List.empty, a, None),
              tuloskirje = tuloskirje,
              ohjeetUudelleOpiskelijalle = Map(),
              hakutoiveet = List.empty,
              haku = None,
              educationBackground = EducationBackground("base_education", false),
              answers = Map(),
              postOffice = None,
              email = Some(a.email),
              requiresAdditionalInfo = false,
              hasForm = true,
              formName = translate(a.formName.getOrElse(Map())),
              requiredPaymentState = None,
              notifications = Map(),
              oppijanumero = henkilo.oppijanumero.getOrElse(personOid)
            )
            Audit.oppija.log(ShowHakemus(request, hakemus.personOid, hakemus.oid, ""))
            HakemusInfo(
              hakemus = hakemus,
              errors = List(),
              questions = List(),
              tulosOk = true,
              paymentInfo = None,
              hakemusSource = "Ataru",
              previewUrl = Some(OphUrlProperties.url("ataru.applications.modify", a.secret))
            )
        }
    }

    private def state(now: Long,
                      haku: Option[Haku],
                      hakukohteet: List[Hakukohde],
                      application: AtaruApplication,
                      valintatulos: Option[Hakemus.Valintatulos]): HakemuksenTila = {
      if (haku.isDefined && hakukohteet.exists(KohteenHakuaika.hakuaikaEnded(haku.get, _, now))) {
        if (haku.get.aikataulu.exists(_.hakukierrosPaattyy.exists(_ < now))) {
          HakukierrosPaattynyt(valintatulos = valintatulos)
        } else if (hakukohteet.forall(KohteenHakuaika.hakuaikaEnded(haku.get, _, now))) {
          HakukausiPaattynyt(valintatulos = valintatulos)
        } else {
          Active(valintatulos = valintatulos)
        }
      } else if (Hakemus.valintatulosHasSomeResults(valintatulos)) {
        HakukausiPaattynyt(valintatulos = valintatulos)
      } else {
        Active()
      }
    }

    private def getHakukohteet(oids: List[String], language: Language): Option[List[Hakukohde]] = {
      def go(oids: List[String], hakukohteet: List[Hakukohde]): Option[List[Hakukohde]] = (oids, hakukohteet) match {
        case (oid :: rest, hks) => tarjontaService.hakukohde(oid, language) match {
          case Some(hk) => go(rest, hk :: hks)
          case None => None
        }
        case (Nil, hks) => Some(hks)
        case _ => None
      }
      go(oids, Nil)
    }

    private def toHakutoive(hakukohde: Hakukohde): Hakutoive = {
      Hakutoive(
        Some(Map("Koulutus-id" -> hakukohde.oid)),
        Some(hakukohde.yhdenPaikanSaanto),
        hakukohde.koulutuksenAlkaminen,
        hakukohde.hakuaikaId,
        hakukohde.hakukohdekohtaisetHakuajat
      )
    }
  }

  // feels a bit strange to have the testing code mixed with the production code,
  // but I am only following the pattern used in the rest of this application
  class StubbedAtaruService extends AtaruServiceCommons {
    implicit private val formats = DefaultFormats +
      FieldSerializer[AtaruApplication](
        renameTo("formName", "form-name"), renameFrom("form-name", "formName")
      )

    def getApplications(personOid: String): List[AtaruApplication] = {
      personOid match {
        case "1.2.246.562.24.14229104473" => {
          val text = scala.io.Source.fromInputStream(getClass.getResourceAsStream("/hakemuseditorimockdata/applications-ataru.json")).mkString
          val allTestApplications = parse(text, useBigDecimalForDouble = false).extract[Option[List[AtaruApplication]]].getOrElse(List())
          allTestApplications
        }
        case _ => List()
      }
    }
  }

  class RemoteAtaruService(config: AppConfig) extends AtaruServiceCommons {
    private val casConfig: CasConfig = new CasConfig.CasConfigBuilder(
      config.settings.securitySettings.casVirkailijaUsername,
      config.settings.securitySettings.casVirkailijaPassword,
      OphUrlProperties.url("cas.url"),
      OphUrlProperties.url("url-ataru-service"),
      AppConfig.callerId,
      AppConfig.callerId,
      "auth/cas")
      .setJsessionName("JSESSIONID").build

    private val casClient: CasClient = CasClientBuilder.build(casConfig)

    implicit private val formats = DefaultFormats +
      FieldSerializer[AtaruApplication](
        renameTo("formName", "form-name"), renameFrom("form-name", "formName")
      )

    def getApplications(personOid: String): List[AtaruApplication] = {
      val req = new RequestBuilder()
        .setMethod("GET")
        .setUrl(OphUrlProperties.url("ataru-service.applications", personOid))
        .build()

      val result = toScala(casClient.execute(req)).flatMap {
        case r if r.getStatusCode == 200 =>
          Future.successful(parse(r.getResponseBodyAsStream()).extract[List[AtaruApplication]])
        case r =>
          Future.failed(new RuntimeException(s"Failed to get applications for $personOid: ${r.toString}"))
      }

      Try(Await.result(result, Duration(10, TimeUnit.SECONDS))) match {
        case Success(applications) => applications
        case Failure(ex) =>
          logger.error(s"Error fetching applications for $personOid", ex)
          throw ex
      }
    }
  }
}

trait AtaruService {
  def findApplications(request: HttpServletRequest, personOid: String, valintatulosFetchStrategy: ValintatulosFetchStrategy, language: Language): List[HakemusInfo]
}
