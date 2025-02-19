package fi.vm.sade.hakemuseditori

import fi.vm.sade.ataru.AtaruServiceComponent
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain.ValidationError
import fi.vm.sade.hakemuseditori.localization.TranslationsComponent
import fi.vm.sade.hakemuseditori.ohjausparametrit.OhjausparametritComponent
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.TarjontaComponent
import fi.vm.sade.hakemuseditori.tarjonta.kouta.RemoteKoutaComponent
import fi.vm.sade.hakemuseditori.tarjonta.vanha.RemoteTarjontaComponent
import fi.vm.sade.hakemuseditori.user.User
import fi.vm.sade.hakemuseditori.valintatulokset.ValintatulosServiceComponent
import fi.vm.sade.hakemuseditori.viestintapalvelu.{TuloskirjeComponent, TuloskirjeKind}
import fi.vm.sade.omatsivut.util.Logging

import javax.servlet.http.HttpServletRequest

import scala.util.{Failure, Success, Try}

sealed trait HakemusResult
case class FullSuccess(hakemukset: List[HakemusInfo]) extends HakemusResult
case class FullFailure(exceptions: List[Throwable]) extends HakemusResult

trait HakemusEditoriComponent extends AtaruServiceComponent
  with OppijanumerorekisteriComponent
  with RemoteTarjontaComponent
  with RemoteKoutaComponent
  with TarjontaComponent
  with OhjausparametritComponent
  with ValintatulosServiceComponent
  with Logging
  with TuloskirjeComponent
  with TranslationsComponent {

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
        case FullFailure(ts) => throw ts.head
      }
      hakemukset.flatMap(hakemus => tuloskirjeService.fetchTuloskirje(request, hakuOid, hakemus.hakemus.oid, tuloskirjeKind))
    }

    def fetchByPersonOid(request: HttpServletRequest,
                         personOid: String,
                         valintatulosFetchStrategy: ValintatulosFetchStrategy): HakemusResult = {
      Try(ataruService.findApplications(request, personOid, valintatulosFetchStrategy, language)) match {
        case Success(applications) => FullSuccess(applications.sortBy(_.hakemus.received).reverse)
        case Failure(exception) => FullFailure(List(exception))
      }
    }

    def fetchByHakemusOid(request: HttpServletRequest,
                          personOid: String,
                          hakemusOid: String,
                          valintatulosFetchStrategy: ValintatulosFetchStrategy): Option[HakemusInfo] = {
      val ataruApplications = ataruService.findApplications(request, personOid, valintatulosFetchStrategy, language)
      val matchingAtaruApplication = ataruApplications.find(_.hakemus.oid == hakemusOid)
      if (ataruApplications.isEmpty) {
        logger.info(s"fetchByHakemusOid(): Ataru returned no applications for given personOid $personOid")
      } else if (matchingAtaruApplication.isEmpty) {
        logger.info(s"fetchByHakemusOid(): Ataru returned applications for personOid $personOid but " +
          s"their hakemusOids ${ataruApplications.map(_.hakemus.oid).mkString(",")} did not match the given hakemusOid $hakemusOid")
      }
      matchingAtaruApplication
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




