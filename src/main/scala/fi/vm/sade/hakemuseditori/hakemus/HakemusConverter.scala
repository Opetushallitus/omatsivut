package fi.vm.sade.hakemuseditori.hakemus

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.hakemus.domain.Hakemus._
import fi.vm.sade.hakemuseditori.hakemus.domain._
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.oppijanumerorekisteri.OppijanumerorekisteriComponent
import fi.vm.sade.hakemuseditori.tarjonta.domain.{Haku, KohteenHakuaika}
import fi.vm.sade.hakemuseditori.tarjonta.{TarjontaComponent, TarjontaService}
import org.apache.commons.lang3.StringUtils
import org.joda.time.LocalDateTime
import org.json4s._

import java.util
import scala.collection.JavaConversions._
import scala.util.Try

trait HakemusConverterComponent {
  this: TarjontaComponent with OppijanumerorekisteriComponent =>

  val hakemusConverter: HakemusConverter
  val tarjontaService: TarjontaService

  class HakemusConverter extends JsonFormats {
    val educationPhaseKey = "koulutustausta" // OppijaConstants.PHASE_EDUCATION
    val baseEducationKey = "POHJAKOULUTUS" // OppijaConstants.ELEMENT_ID_BASE_EDUCATION
    val preferencePhaseKey = "hakutoiveet" // OppijaConstants.PHASE_APPLICATION_OPTIONS
    val requiredBaseEducationsKey = "Koulutus-requiredBaseEducations"

    def convertToHakemus(tuloskirje: Option[Tuloskirje], haku: Haku, application: ImmutableLegacyApplicationWrapper)(implicit lang: Language.Language) : Hakemus = {
      convertToHakemus(tuloskirje, haku, application, None)
    }

    def convertToHakemus(tuloskirje: Option[Tuloskirje], haku: Haku, application: ImmutableLegacyApplicationWrapper, valintatulos: Option[Valintatulos])(implicit lang: Language.Language) : Hakemus = {
      val koulutusTaustaAnswers: util.Map[String, String] = application.phaseAnswers(educationPhaseKey)
      val receivedTime =  application.received.map(_.getTime)
      val answers = application.answers
      val hakutoiveet = convertHakuToiveet(application)

      val ohjeetUudelleOpiskelijalleMap: Map[String, String] = hakutoiveet
        .filter(hakutoive => {
          val hakukohdeOid: Option[String] = hakutoive.hakemusData.map(_("Koulutus-id"))
          tarjontaService.getOhjeetUudelleOpiskelijalle(hakukohdeOid, lang).nonEmpty
        })
        .map(hakutoive => {
          val hakukohdeOid: Option[String] = hakutoive.hakemusData.map(_("Koulutus-id"))
          hakukohdeOid.getOrElse("") -> tarjontaService.getOhjeetUudelleOpiskelijalle(hakukohdeOid, lang).getOrElse("")
        }).toMap

      val henkilo = oppijanumerorekisteriService.henkilo(application.personOid)

      Hakemus(
        application.oid,
        application.personOid,
        receivedTime,
        application.updated.map(_.getTime).orElse(receivedTime),
        tila(haku, application, hakutoiveet, valintatulos),
        tuloskirje,
        ohjeetUudelleOpiskelijalleMap,
        hakutoiveet,
        Some(haku),
        EducationBackground(koulutusTaustaAnswers.get(baseEducationKey), !Try {koulutusTaustaAnswers.get("ammatillinenTutkintoSuoritettu").toBoolean}.getOrElse(false)),
        answers,
        None,
        application.sähköposti,
        false,
        false,
        None,
        Map.empty,
        henkilo.oppijanumero.getOrElse(application.personOid),
        None
      )
    }

    def tila(haku: Haku, application: ImmutableLegacyApplicationWrapper, hakutoiveet: List[Hakutoive], origValintatulos: Option[Valintatulos])(implicit lang: Language.Language): HakemuksenTila = {
      val valintatulos = insertHakutoiveNimiToValintatulosIfNotPresent(origValintatulos, hakutoiveet)
      val now = new LocalDateTime().toDate.getTime // Use LocalDateTime so that we can use TimeWarp in tests
      if (hakutoiveet.exists(KohteenHakuaika.hakuaikaEnded(haku, _, now))) {
        if (haku.aikataulu.flatMap(_.hakukierrosPaattyy.map(_ < now)).getOrElse(false)) {
          HakukierrosPaattynyt(valintatulos = valintatulos)
        }
        else if (hakutoiveet
          .filter(_.hakemusData.isDefined)
          .forall(KohteenHakuaika.hakuaikaEnded(haku, _, now))) {
          HakukausiPaattynyt(valintatulos = valintatulos)
        } else {
          Active(valintatulos = valintatulos)
        }
      } else {
        application.state match {
          case "ACTIVE" => if (valintatulosHasSomeResults(valintatulos)) HakukausiPaattynyt(valintatulos = valintatulos) else Active()
          case "PASSIVE" => Passive()
          case "INCOMPLETE" => Incomplete()
          case "SUBMITTED" => Submitted()
          case x => {
            throw new RuntimeException("Unexpected state for application " + application.oid + ": " + x)
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

    private def convertHakuToiveet(application: ImmutableLegacyApplicationWrapper)(implicit lang: Language): List[Hakutoive] = {
      def hakutoiveDataToHakutoive(data: HakutoiveData): Hakutoive = {
        data.isEmpty match {
          case true =>
            Hakutoive(None, None, None, None, None)
          case _ =>
            val tarjonnanHakukohde = tarjontaService.hakukohde(data("Koulutus-id"), lang)
            val amendedData = amendWithKoulutusInformaatio(lang, data)

            Hakutoive(Some(amendedData), tarjonnanHakukohde.map(_.yhdenPaikanSaanto), tarjonnanHakukohde.flatMap(_.koulutuksenAlkaminen),
                      tarjonnanHakukohde.flatMap(_.hakuaikaId), tarjonnanHakukohde.flatMap(_.hakukohdekohtaisetHakuajat))
        }
      }
      HakutoiveetConverter.convertFromAnswers(application.answers).map(hakutoiveDataToHakutoive)
    }

    private def amendWithKoulutusInformaatio(lang: Language, data: HakutoiveData): HakutoiveData = {
      // TODO onko tämä tarpeen?
      val koulutusOption = data.get("Koulutus")
      val koulutus = koulutusOption match {
        case Some(k) if StringUtils.isBlank(k) => tarjontaService.hakukohde(data("Koulutus-id"), lang).map(_.nimi)
        case None => tarjontaService.hakukohde(data("Koulutus-id"), lang).map(_.nimi)
        case default@_ => default
      }
      val opetuspiste = data.get("Opetuspiste").orElse(None)
      val amendedData = data ++ koulutus.map("Koulutus" -> _) ++ opetuspiste.map("Opetuspiste" -> _)
      amendedData
    }
  }
}


