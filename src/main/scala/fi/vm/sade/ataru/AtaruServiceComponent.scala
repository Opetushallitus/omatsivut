package fi.vm.sade.ataru

import java.time.Instant
import java.util.concurrent.TimeUnit
import fi.vm.sade.hakemuseditori.auditlog.{Audit, ShowHakemus}
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, ValintatulosFetchStrategy}
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, EducationBackground, HakemuksenTila, Hakemus, HakukausiPaattynyt, HakukierrosPaattynyt, Hakutoive}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakukohde, KohteenHakuaika}
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.{AccessibleHtml, Pdf, TuloskirjeComponent}
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}

import javax.servlet.http.HttpServletRequest
import org.http4s.{Request, Uri}
import org.http4s.Method.GET
import org.http4s.client.blaze
import org.joda.time.LocalDateTime
import org.json4s.FieldSerializer.{renameFrom, renameTo}
import org.json4s.{DefaultFormats, FieldSerializer}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.parse
import scalaz.concurrent.Task

import scala.concurrent.duration.Duration
import scala.util.Try

case class AtaruApplication(oid: String,
                            secret: String,
                            email: String,
                            haku: Option[String],
                            hakukohteet: List[String],
                            submitted: String,
                            formName: Map[String, String])

trait AtaruServiceComponent  {
  this: LomakeRepositoryComponent
    with TarjontaComponent
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

      getApplications(personOid)
        .map(a => (
          a,
          a.haku.flatMap(tarjontaService.haku(_, language)),
          getHakukohteet(a.hakukohteet, language),
          a.haku.flatMap(tuloskirjeService.getTuloskirjeInfo(request, _, a.oid, AccessibleHtml))
        ))
        .collect {
          case (a, Some(haku), Some(hakukohteet), tuloskirje) =>
            val valintatulos = Try(if (a.haku.isDefined && valintatulosFetchStrategy.ataru(haku, henkilo, a)) {
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
              formName = None
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
              formName = a.formName.get(language.toString),
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
          val text = io.Source.fromInputStream(getClass.getResourceAsStream("/hakemuseditorimockdata/applications-ataru.json")).mkString
          val allTestApplications = parse(text, useBigDecimalForDouble = false).extract[Option[List[AtaruApplication]]].getOrElse(List())
          allTestApplications
        }
        case _ => List()
      }
    }
  }

  class RemoteAtaruService(config: AppConfig, casVirkailijaClient: CasClient) extends AtaruServiceCommons {
    private val casParams = CasParams(
      OphUrlProperties.url("url-ataru-service"),
      "auth/cas",
      config.settings.securitySettings.casVirkailijaUsername,
      config.settings.securitySettings.casVirkailijaPassword
    )
    private val httpClient = CasAuthenticatingClient(
      casVirkailijaClient,
      casParams,
      blaze.defaultClient,
      AppConfig.callerId,
      "ring-session"
    )

    implicit private val formats = DefaultFormats +
      FieldSerializer[AtaruApplication](
        renameTo("formName", "form-name"), renameFrom("form-name", "formName")
      )

    def getApplications(personOid: String): List[AtaruApplication] = {
      Uri.fromString(OphUrlProperties.url("ataru-service.applications", personOid))
        .fold(Task.fail, uri => {
          httpClient.fetch(Request(method = GET, uri = uri)) {
            case r if r.status.code == 200 => r.as[String].map(s => JsonMethods.parse(s).extract[List[AtaruApplication]])
            case r => Task.fail(new RuntimeException(s"Failed to get applications for $personOid: ${r.toString()}"))
          }
        }).attemptRunFor(Duration(10, TimeUnit.SECONDS)).fold(throw _, x => x)
    }
  }
}

trait AtaruService {
  def findApplications(request: HttpServletRequest, personOid: String, valintatulosFetchStrategy: ValintatulosFetchStrategy, language: Language): List[HakemusInfo]
}
