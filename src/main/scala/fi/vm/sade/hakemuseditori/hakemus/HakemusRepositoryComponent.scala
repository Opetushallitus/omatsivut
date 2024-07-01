package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.hakemuseditori.auditlog._
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.ApplicationDao
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.hakemuseditori.tarjonta.{TarjontaComponent, TarjontaService}
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.{Pdf, TuloskirjeComponent}
import fi.vm.sade.utils.Timer._

import javax.servlet.http.HttpServletRequest
import scala.util.{Failure, Success, Try}

trait HakemusRepositoryComponent {
  this: HakemusConverterComponent
    with SpringContextComponent with TarjontaComponent with OhjausparametritComponent with TuloskirjeComponent
    with ValintatulosServiceComponent =>

  val tarjontaService: TarjontaService
  val hakemusRepository = new HakemusFinder

  private val dao = new ApplicationDao()


  class HakemusFinder {

    def fetchHakemukset(request: HttpServletRequest,
                        personOid: String,
                        valintatulosFetchStrategy: ValintatulosFetchStrategy)
                       (implicit lang: Language.Language): List[HakemusInfo] = {
      val legacyApplications: List[ImmutableLegacyApplicationWrapper] = timed("Application fetch DAO", 1000) {
        dao.findByPersonOid(personOid)
      }.map(ImmutableLegacyApplicationWrapper.wrap)
      fetchHakemukset(request, legacyApplications, valintatulosFetchStrategy)
    }

    def getHakemus(request: HttpServletRequest,
                   hakemusOid: String,
                   valintatulosFetchStrategy: ValintatulosFetchStrategy)
                  (implicit lang: Language): Option[HakemusInfo] = {
      val legacyApplications: List[ImmutableLegacyApplicationWrapper] = timed("Application fetch DAO", 1000) {
        dao.findByOid(hakemusOid).toList
      }.map(ImmutableLegacyApplicationWrapper.wrap)
      fetchHakemukset(request, legacyApplications, valintatulosFetchStrategy).headOption
    }

    private def fetchHakemukset(request: HttpServletRequest, legacyApplications: List[ImmutableLegacyApplicationWrapper],
                                valintatulosFetchStrategy: ValintatulosFetchStrategy)
                               (implicit lang: Language): List[HakemusInfo] = {
      timed("Application fetch", 1000){
        legacyApplications.filter {
          application => {
            !application.state.equals("PASSIVE")
          }
        }.flatMap(application => {
          val hakuOption = application.hakuOid match {
            case "" => None
            case hakuOid => timed("LomakeRepository get lomake", 1000) {
              tarjontaService.haku(hakuOid, lang).filter(_.published).filter(_.hakukierrosvoimassa)
            }
          }
          for {
            haku <- hakuOption
          } yield {
            val fetchTulos = valintatulosFetchStrategy.legacy(haku, application)
            val (valintatulos, tulosOk) = if (fetchTulos) {
              timed("fetchHakemukset -> fetchValintatulos", 100) { fetchValintatulos(application, haku) }
            } else {
              (None, true)
            }
            val letterForHaku = tuloskirjeService.getTuloskirjeInfo(request, haku.oid, application.oid, Pdf)
            val hakemus = timed("fetchHakemukset -> hakemusConverter.convertToHakemus", 100) { hakemusConverter.convertToHakemus(letterForHaku, haku, application, valintatulos) }
            timed("fetchHakemukset -> auditLogger.log", 100) { Audit.oppija.log(ShowHakemus(request, application.personOid, hakemus.oid, haku.oid)) }
            // enää ei ole aktiivisia hakuja haku-appissa joten lomaketietoja ei tarvitse kaivella
            HakemusInfo(
              hakemus = hakemus,
              errors = List(),
              questions = List(),
              tulosOk = tulosOk,
              paymentInfo = None,
              hakemusSource = "HakuApp",
              previewUrl = hakemus.omatsivutPreviewUrl
            )
          }
        })
      }
    }

    private def fetchValintatulos(application: ImmutableLegacyApplicationWrapper, haku: Haku)(implicit lang: Language) = {
      Try(valintatulosService.getValintatulos(application.oid, haku.oid)) match {
        case Success(t) => (t, true)
        case Failure(e) => (None, false)
      }
    }

  }
}
