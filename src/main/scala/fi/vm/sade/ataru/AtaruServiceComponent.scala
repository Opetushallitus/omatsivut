package fi.vm.sade.ataru

import java.util.concurrent.TimeUnit

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus.{HakemusInfo, ValintatulosFetchStrategy}
import fi.vm.sade.hakemuseditori.hakemus.domain.{Active, EducationBackground, HakemuksenTila, Hakemus, HakukausiPaattynyt, HakukierrosPaattynyt}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakuaika, Hakukohde}
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.TuloskirjeComponent
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.cas.{CasAuthenticatingClient, CasClient, CasParams}
import org.http4s.Method.GET
import org.http4s.client.blaze
import org.http4s.json4s.native.jsonExtract
import org.http4s.{Request, Uri}
import org.joda.time.LocalDateTime
import org.json4s.DefaultFormats

import scala.concurrent.duration.Duration
import scala.util.Try
import scalaz.concurrent.Task

case class AtaruApplication(oid: String,
                            secret: String,
                            haku: String,
                            hakukohteet: List[String])

trait AtaruServiceComponent  {
  this: LomakeRepositoryComponent
    with TarjontaComponent
    with OppijanumerorekisteriComponent
    with ValintatulosServiceComponent
    with TuloskirjeComponent =>

  val ataruService: AtaruService

  class StubbedAtaruService extends AtaruService {
    override def findApplications(personOid: String, valintatulosFetchStrategy: ValintatulosFetchStrategy): List[HakemusInfo] = List()
  }

  class RemoteAtaruService(config: AppConfig) extends AtaruService {
    private val blazeHttpClient = blaze.defaultClient
    private val casClient = new CasClient(config.settings.securitySettings.casUrl, blazeHttpClient)
    private val casParams = CasParams(
      OphUrlProperties.url("url-ataru-service"),
      "auth/cas",
      config.settings.securitySettings.casUsername,
      config.settings.securitySettings.casPassword
    )
    private val httpClient = CasAuthenticatingClient(
      casClient,
      casParams,
      blazeHttpClient,
      Some("omatsivut.omatsivut.backend"),
      "ring-session"
    )

    implicit val formats = DefaultFormats

    private def getApplications(personOid: String): List[AtaruApplication] = {
      Uri.fromString(OphUrlProperties.url("ataru-service.applications", personOid))
        .fold(Task.fail, uri => {
          httpClient.fetch(Request(method = GET, uri = uri)) {
            case r if r.status.code == 200 => r.as[List[AtaruApplication]](jsonExtract[List[AtaruApplication]])
            case r => Task.fail(new RuntimeException(s"Failed to get applications for $personOid: ${r.toString()}"))
          }
        }).attemptRunFor(Duration(10, TimeUnit.SECONDS)).fold(throw _, x => x)
    }

    private def state(now: Long,
                      haku: Haku,
                      hakukohteet: List[Hakukohde],
                      application: AtaruApplication,
                      valintatulos: Option[Hakemus.Valintatulos]): HakemuksenTila = {
      if (Hakuaika.anyApplicationPeriodEnded(haku, hakukohteet.map(_.kohteenHakuaika), now)) {
        if (haku.aikataulu.exists(_.hakukierrosPaattyy.exists(_ < now))) {
          HakukierrosPaattynyt(valintatulos = valintatulos)
        } else if (!haku.active) {
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

    private def getHakukohteet(oids: List[String]): Option[List[Hakukohde]] = {
      def go(oids: List[String], hakukohteet: List[Hakukohde]): Option[List[Hakukohde]] = (oids, hakukohteet) match {
        case (oid :: rest, hks) => tarjontaService.hakukohde(oid) match {
          case Some(hk) => go(rest, hk :: hks)
          case None => None
        }
        case (Nil, hks) => Some(hks)
        case _ => None
      }
      go(oids, Nil)
    }

    def findApplications(personOid: String,
                         valintatulosFetchStrategy: ValintatulosFetchStrategy): List[HakemusInfo] = {
      val now = new LocalDateTime().toDate.getTime
      val henkilo = oppijanumerorekisteriService.henkilo(personOid)
      getApplications(personOid)
        .map(a => (
          a,
          tarjontaService.haku(a.haku, Language.fi),
          getHakukohteet(a.hakukohteet),
          Try(
            if (valintatulosFetchStrategy.ataru(a, henkilo)) {
              valintatulosService.getValintatulos(a.oid, a.haku)
            } else {
              None
            }
          ),
          tuloskirjeService.getTuloskirjeInfo(a.haku, a.oid)
        ))
        .collect {
          case (a, Some(haku), Some(hakukohteet), valintatulos, tuloskirje) =>
            val hakemus = Hakemus(
              oid = a.oid,
              personOid = personOid,
              received = None,
              updated = None,
              state = state(now, haku, hakukohteet, a, valintatulos.getOrElse(None)),
              tuloskirje = tuloskirje,
              hakutoiveet = List(),
              haku = haku,
              educationBackground = EducationBackground("base_education", false),
              answers = Map(),
              postOffice = None,
              email = None, // FIXME
              requiresAdditionalInfo = false,
              hasForm = true,
              requiredPaymentState = None,
              notifications = Map(),
              secret = Option(a.secret)
            )
            HakemusInfo(
              hakemus = hakemus,
              errors = List(),
              questions = List(),
              tulosOk = valintatulos.isSuccess,
              paymentInfo = None,
              hakemusSource = "Ataru",
              ataruHakijaUrl = OphUrlProperties.url("ataru.hakija.url")
            )
        }
    }
  }
}

trait AtaruService {
  def findApplications(personOid: String, valintatulosFetchStrategy: ValintatulosFetchStrategy): List[HakemusInfo]
}
