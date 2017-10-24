package fi.vm.sade.hakemuseditori.hakemus

import java.util

import com.google.common.collect.ImmutableSet
import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koodisto.KoodistoComponent
import fi.vm.sade.hakemuseditori.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.hakemuseditori.lomake.domain.Lomake
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, Hakuaika}
import fi.vm.sade.hakemuseditori.tarjonta.{TarjontaComponent, TarjontaService}
import fi.vm.sade.haku.oppija.hakemus.service.EducationRequirementsUtil._
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.OppijaConstants
import fi.vm.sade.haku.virkailija.lomakkeenhallinta.util.Types.MergedAnswers
import org.apache.commons.lang3.StringUtils
import org.joda.time.LocalDateTime
import org.json4s._

import scala.collection.JavaConversions._
import scala.util.Try

trait HakemusConverterComponent {
  this: KoodistoComponent with TarjontaComponent with KoulutusInformaatioComponent =>

  val hakemusConverter: HakemusConverter
  val tarjontaService: TarjontaService

  class HakemusConverter extends JsonFormats {
    val educationPhaseKey = OppijaConstants.PHASE_EDUCATION
    val baseEducationKey = OppijaConstants.ELEMENT_ID_BASE_EDUCATION
    val preferencePhaseKey = OppijaConstants.PHASE_APPLICATION_OPTIONS
    val requiredBaseEducationsKey = "Koulutus-requiredBaseEducations"

    def convertToHakemus(tuloskirje: Option[Tuloskirje], lomake: Option[Lomake], haku: Haku, application: ImmutableLegacyApplicationWrapper)(implicit lang: Language.Language) : Hakemus = {
      convertToHakemus(tuloskirje, lomake, haku, application, None)
    }

    def convertToHakemus(tuloskirje: Option[Tuloskirje], lomake: Option[Lomake], haku: Haku, application: ImmutableLegacyApplicationWrapper, valintatulos: Option[Valintatulos])(implicit lang: Language.Language) : Hakemus = {
      val koulutusTaustaAnswers: util.Map[String, String] = application.phaseAnswers(educationPhaseKey)
      val receivedTime =  application.received.map(_.getTime)
      val answers = application.answers
      val hakutoiveet = convertHakuToiveet(application, lomake)

      def hasBaseEducationConflict(hakutoive: Hakutoive) = {
        haku.checkBaseEducationConflict match {
          case false => false
          case true => hakutoive.hakemusData.get.get(requiredBaseEducationsKey) match {
            case Some(requirements) if !requirements.isEmpty =>
              !answersFulfillBaseEducationRequirements(MergedAnswers.of(koulutusTaustaAnswers), ImmutableSet.copyOf(requirements.split(",")))
            case _ => false
          }
        }
      }

      val notifications = hakutoiveet.filter(p => p.hakemusData.isDefined).map(hakutoive => {
        val oid = hakutoive.hakemusData.get(OppijaConstants.PREFERENCE_FRAGMENT_OPTION_ID)
        oid -> Map(
          "baseEducationConflict" -> hasBaseEducationConflict(hakutoive)
        )
      }).toMap

      Hakemus(
        application.oid,
        receivedTime,
        application.updated.map(_.getTime).orElse(receivedTime),
        tila(haku, application, hakutoiveet, valintatulos),
        tuloskirje,
        hakutoiveet,
        haku,
        EducationBackground(koulutusTaustaAnswers.get(baseEducationKey), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false)),
        answers,
        answers.get("henkilotiedot")
          .flatMap(_.get("Postinumero"))
          .flatMap(koodistoService.postOfficeTranslations)
          .flatMap((translations: Map[String,String]) => translations.get(lang.toString)),
        application.sähköposti,
        lomake.map(_.requiresAdditionalInfo(application)).getOrElse(false),
        lomake.isDefined,
        application.requiredPaymentState,
        notifications
      )
    }

    def tila(haku: Haku, application: ImmutableLegacyApplicationWrapper, hakutoiveet: List[Hakutoive], origValintatulos: Option[Valintatulos])(implicit lang: Language.Language): HakemuksenTila = {
      val valintatulos = insertHakutoiveNimiToValintatulosIfNotPresent(origValintatulos, hakutoiveet)

      if (application.isPostProcessing) {
        PostProcessing()
      } else {
        val now = new LocalDateTime().toDate.getTime // Use LocalDateTime so that we can use TimeWarp in tests
        if (Hakuaika.anyApplicationPeriodEnded(haku, hakutoiveet.map(_.kohdekohtainenHakuaika), now)) {
          if (haku.aikataulu.flatMap(_.hakukierrosPaattyy.map(_ < now)).getOrElse(false)) {
            HakukierrosPaattynyt(valintatulos = valintatulos)
          }
          else if (!haku.active) {
            HakukausiPaattynyt(valintatulos = valintatulos)
          } else {
            Active(valintatulos = valintatulos)
          }
        } else {
          application.state match {
            case "ACTIVE" => if(valintatulosHasSomeResults(valintatulos)) HakukausiPaattynyt(valintatulos = valintatulos) else Active()
            case "PASSIVE" => Passive()
            case "INCOMPLETE" => Incomplete()
            case "SUBMITTED" => Submitted()
            case x => {
              throw new RuntimeException("Unexpected state for application " + application.oid + ": " + x)
            }
          }
        }
      }
    }

    def insertHakutoiveNimiToValintatulosIfNotPresent(valintatulos: Option[Valintatulos], hakutoiveet: List[Hakutoive]): Option[Valintatulos] = {
      def getHakutoiveData(oid:String): HakutoiveData = {
        hakutoiveet.find(p => p.hakemusData.exists(d => d.get("Koulutus-id").getOrElse("") == oid)) match {
          case None => Map()
          case Some(x) => x.hakemusData.get
        }
      }
      valintatulos.map(v => {
        v transformField {
          case ("hakutoiveet", a:JArray) => ("hakutoiveet", JArray(a.arr.map(ht => {
            val hakukohdeOid = ( ht \ "hakukohdeOid" ).extract[String]
            val hakukohdeData = getHakutoiveData(hakukohdeOid)
            ht transformField {
              case JField("hakukohdeNimi", JString(s)) if StringUtils.isBlank(s) => JField("hakukohdeNimi", JString(hakukohdeData.getOrElse("Koulutus", s)))
              case JField("tarjoajaNimi", JString(s)) if StringUtils.isBlank(s) => JField("tarjoajaNimi", JString(hakukohdeData.getOrElse("Opetuspiste", s)))
            }
          })))
        }
      }
      )
    }

    def anyApplicationPeriodEnded(haku: Haku, application: ImmutableLegacyApplicationWrapper, lomake: Option[Lomake])(implicit lang: Language): Boolean = {
      val now = new LocalDateTime().toDate.getTime // Use LocalDateTime so that we can use TimeWarp in tests
      Hakuaika.anyApplicationPeriodEnded(haku, convertHakuToiveet(application, lomake).map(_.kohdekohtainenHakuaika), now)
    }

    private def convertHakuToiveet(application: ImmutableLegacyApplicationWrapper, lomake: Option[Lomake])(implicit lang: Language): List[Hakutoive] = {
      def hakutoiveDataToHakutoive(data: HakutoiveData): Hakutoive = {
        data.isEmpty match {
          case true =>
            Hakutoive.empty
          case _ =>
            val tarjonnanHakukohde = tarjontaService.hakukohde(data("Koulutus-id"))
            val amendedData = amendWithKoulutusInformaatio(lang, data)

            Hakutoive(Some(amendedData), tarjonnanHakukohde.flatMap(_.hakuaikaId), tarjonnanHakukohde.flatMap(_.kohteenHakuaika))
        }
      }
      val maxHakutoiveet = if (lomake.nonEmpty) {
        Some(lomake.get.maxHakutoiveet)
      } else {
        None
      }
      HakutoiveetConverter.convertFromAnswers(application.answers, maxHakutoiveet).map(hakutoiveDataToHakutoive)
    }

    private def amendWithKoulutusInformaatio(lang: Language, data: HakutoiveData): HakutoiveData = {
      val koulutus = data.get("Koulutus").orElse(koulutusInformaatioService.koulutus(data("Koulutus-id"), lang).map(_.name))
      val opetuspiste = data.get("Opetuspiste").orElse(koulutusInformaatioService.opetuspiste(data("Opetuspiste-id"), lang).map(_.name))

      val amendedData = data ++ koulutus.map("Koulutus" -> _) ++ opetuspiste.map("Opetuspiste" -> _)
      amendedData
    }
  }
}


