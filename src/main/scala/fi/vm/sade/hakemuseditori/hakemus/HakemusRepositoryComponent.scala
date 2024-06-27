package fi.vm.sade.hakemuseditori.hakemus

import java.util.Date
import fi.vm.sade.hakemuseditori.SendMailComponent
import fi.vm.sade.hakemuseditori.auditlog._
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.ImmutableLegacyApplicationWrapper.{LegacyApplicationAnswers, wrap}
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus.Answers
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.hakemus.hakuapp.{Application, ApplicationDao}
import fi.vm.sade.hakemuseditori.hakumaksu.HakumaksuComponent
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.lomake.domain.Lomake
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.Haku
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.{Pdf, TuloskirjeComponent}
import fi.vm.sade.utils.Timer._
import fi.vm.sade.utils.slf4j.Logging

import javax.servlet.http.HttpServletRequest
import org.joda.time.LocalDateTime

import scala.util.{Failure, Success, Try}

trait HakemusRepositoryComponent {
  this: LomakeRepositoryComponent with ApplicationValidatorComponent with HakemusConverterComponent
    with SpringContextComponent with TarjontaComponent with OhjausparametritComponent with TuloskirjeComponent
    with ValintatulosServiceComponent with HakumaksuComponent with SendMailComponent =>

  import scala.collection.JavaConversions._

  val hakemusRepository = new HakemusFinder
  val applicationRepository = new ApplicationFinder

  private val dao = new ApplicationDao()


  class ApplicationFinder {
    def findStoredApplicationByOid(oid: String): Option[ImmutableLegacyApplicationWrapper] = {
      dao.findByOid(oid).map(wrap)
    }

    def exists(personOid: String, hakemusOid: String) = {
      findStoredApplicationByPersonAndOid(personOid, hakemusOid).isDefined
    }

    def findStoredApplicationByPersonAndOid(personOid: String, oid: String) = {
      findStoredApplicationByOid(oid).filter(application => personOid.equals(application.personOid))
    }

    def applicationsByPersonOid(personOid: String): Iterable[ImmutableLegacyApplicationWrapper] = {
      timed("Application fetch DAO", 1000)(dao.findByPersonOid(personOid)).map(wrap)
    }

  }

  class HakemusFinder {
    private val applicationValidator: ApplicationValidator = newApplicationValidator

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
          val (lomakeOption, hakuOption) = timed("LomakeRepository get lomake", 1000) {
            lomakeRepository.lomakeAndHakuByApplication(application)
          }
          for {
            haku <- hakuOption
          } yield {
            val fetchTulos = valintatulosFetchStrategy.legacy(haku, application)
            val (valintatulos, tulosOk) = if (fetchTulos) {
              timed("fetchHakemukset -> fetchValintatulos", 100) { fetchValintatulos(application, haku, lomakeOption) }
            } else {
              (None, true)
            }
            val letterForHaku = tuloskirjeService.getTuloskirjeInfo(request, haku.oid, application.oid, Pdf)
            val hakemus = timed("fetchHakemukset -> hakemusConverter.convertToHakemus", 100) { hakemusConverter.convertToHakemus(letterForHaku, lomakeOption, haku, application, valintatulos) }
            timed("fetchHakemukset -> auditLogger.log", 100) { Audit.oppija.log(ShowHakemus(request, application.personOid, hakemus.oid, haku.oid)) }

            lomakeOption match {
              case Some(lomake) if haku.applicationPeriods.exists(_.active) =>
                timed("fetchHakemukset -> applicationValidator.validateAndFindQuestions", 100) { applicationValidator.validateAndFindQuestions(haku, lomake, withNoPreferenceSpesificAnswers(hakemus), application) match {
                    case (app, errors, questions) =>
                      val hakemus = hakemusConverter.convertToHakemus(letterForHaku, Some(lomake), haku, app, valintatulos)
                      HakemusInfo(
                        hakemus = hakemus,
                        errors = errors,
                        questions = questions,
                        tulosOk = tulosOk,
                        paymentInfo = None,
                        hakemusSource = "HakuApp",
                        previewUrl = hakemus.omatsivutPreviewUrl
                      )
                  }
                }
              case _ =>
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
          }
        })
      }
    }

    private def fetchValintatulos(application: ImmutableLegacyApplicationWrapper, haku: Haku, lomake: Option[Lomake])(implicit lang: Language) = {
      Try(valintatulosService.getValintatulos(application.oid, haku.oid)) match {
        case Success(t) => (t, true)
        case Failure(e) => (None, false)
      }
    }

    private def withNoPreferenceSpesificAnswers(hakemus: Hakemus): HakemusLike = {
      hakemus.toHakemusMuutos.copy(answers = hakemus.answers.filterKeys(!_.equals(HakutoiveetConverter.hakutoiveetPhase)))
    }
  }
}
