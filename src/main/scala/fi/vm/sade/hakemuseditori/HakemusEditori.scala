package fi.vm.sade.hakemuseditori

import fi.vm.sade.ataru.AtaruServiceComponent
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain.{ValidationError}
import fi.vm.sade.hakemuseditori.localization.{TranslationsComponent}
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.kouta.RemoteKoutaComponent
import fi.vm.sade.hakemuseditori.tarjonta.vanha.RemoteTarjontaComponent
import fi.vm.sade.hakemuseditori.user.User
import fi.vm.sade.hakemuseditori.valintatulokset.{ValintatulosServiceComponent}
import fi.vm.sade.hakemuseditori.viestintapalvelu.{TuloskirjeComponent, TuloskirjeKind}
import fi.vm.sade.omatsivut.util.Logging

import javax.servlet.http.HttpServletRequest

import scala.util.{Failure, Success, Try}

sealed trait HakemusResult
case class FullSuccess(hakemukset: List[HakemusInfo]) extends HakemusResult
case class PartialSuccess(hakemukset: List[HakemusInfo], exceptions: List[Throwable]) extends HakemusResult
case class FullFailure(exceptions: List[Throwable]) extends HakemusResult

trait HakemusEditoriComponent extends AtaruServiceComponent
  with OppijanumerorekisteriComponent
  with RemoteTarjontaComponent
  with RemoteKoutaComponent
  with TarjontaComponent
  with OhjausparametritComponent
  with HakemusRepositoryComponent
  with ValintatulosServiceComponent
  with Logging
  with TuloskirjeComponent
  with TranslationsComponent
  with SpringContextComponent
  with HakemusConverterComponent {

  def newEditor(userContext: HakemusEditoriUserContext): HakemusEditori = {
    new HakemusEditori {
      override implicit def language = userContext.language
      override def user() = userContext.user()
    }
  }

  trait HakemusEditori {
    implicit def language: Language.Language
    def user(): User

    def fetchTuloskirje(request: HttpServletRequest, personOid: String, hakuOid: String, tuloskirjeKind: TuloskirjeKind): Option[Array[Byte]] = {
      val hakemukset = fetchByPersonOid(request, personOid, DontFetch) match {
        case FullSuccess(hs) => hs.find((h: HakemusInfo) => h.hakemus.haku.isDefined && h.hakemus.haku.get.oid == hakuOid)
        case PartialSuccess(_, ts) => throw ts.head
        case FullFailure(ts) => throw ts.head
      }
      hakemukset.flatMap(hakemus => tuloskirjeService.fetchTuloskirje(request, hakuOid, hakemus.hakemus.oid, tuloskirjeKind))
    }

    def fetchByPersonOid(request: HttpServletRequest,
                         personOid: String,
                         valintatulosFetchStrategy: ValintatulosFetchStrategy): HakemusResult = {
      val ataruHakemukset = Try(ataruService.findApplications(request, personOid, valintatulosFetchStrategy, language))
      val hakuAppHakemukset = oppijanumerorekisteriService.fetchAllDuplicateOids(personOid).toList
        .map(oid => Try(hakemusRepository.fetchHakemukset(request, oid, valintatulosFetchStrategy)))
      (ataruHakemukset :: hakuAppHakemukset).foldLeft((List.empty[HakemusInfo], List.empty[Throwable])) {
        case ((hs, ts), Success(hhs)) => (hs ::: hhs, ts)
        case ((hs, ts), Failure(t)) => (hs, t :: ts)
      } match {
        case (hs, Nil) => FullSuccess(hs.sortBy(_.hakemus.received).reverse)
        case (Nil, ts) => FullFailure(ts)
        case (hs, ts) => PartialSuccess(hs.sortBy(_.hakemus.received).reverse, ts)
      }
    }

    def fetchByHakemusOid(request: HttpServletRequest,
                          personOid: String,
                          hakemusOid: String,
                          valintatulosFetchStrategy: ValintatulosFetchStrategy): Option[HakemusInfo] = {
      val ataruApplications = ataruService.findApplications(request, personOid, valintatulosFetchStrategy, language)
      val matchingAtaruApplication = ataruApplications.find(_.hakemus.oid == hakemusOid)
      if (ataruApplications.isEmpty) {
        logger.info(s"fetchByHakemusOid(): Ataru returned no applications for given personOid $personOid, searching from haku-app.")
      } else if (matchingAtaruApplication.isEmpty) {
        logger.info(s"fetchByHakemusOid(): Ataru returned applications for personOid $personOid but " +
          s"their hakemusOids ${ataruApplications.map(_.hakemus.oid).mkString(",")} did not match the given hakemusOid $hakemusOid")
      }
      val result = matchingAtaruApplication.orElse {
        // jos ei löydy atarusta, haetaan haku-appista
        val optFromHakemusRepository = hakemusRepository.getHakemus(request, hakemusOid, valintatulosFetchStrategy)
        if (optFromHakemusRepository.isEmpty) {
          logger.info(s"fetchByHakemusOid(): Haku-app returned no applications for given hakemusOid $hakemusOid.")
        }
        val matchingFromHakemusRepository = optFromHakemusRepository.filter { hakemus =>
          val personOidFromHakemus = hakemus.hakemus.personOid
          val oidsMatch = personOidFromHakemus == personOid
          if (!oidsMatch) {
            logger.warn(s"fetchByHakemusOid(): Haku-app returned an application for hakemusOid $hakemusOid but its personOid $personOidFromHakemus" +
              s"did not match the given personOid parameter $personOid")
          }
          oidsMatch
        }
        matchingFromHakemusRepository
      }
      if (result.isEmpty){
          logger.warn(s"fetchByHakemusOid(): neither ataru nor hakemus repository returned a " +
            s"matching application for personOid $personOid, hakemusOid $hakemusOid")
      }
      result
    }

  }
}

trait HakemusEditoriUserContext {
  def language: Language.Language
  def user(): User
}

class ForbiddenException() extends RuntimeException("Forbidden")
class ValidationException(errors: List[ValidationError]) extends RuntimeException("Validation failed") {
  val validationErrors = errors
}




