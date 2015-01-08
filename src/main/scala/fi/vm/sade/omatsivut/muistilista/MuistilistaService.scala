package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.omatsivut.domain.Language
import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.omatsivut.koulutusinformaatio.domain.Koulutus
import fi.vm.sade.omatsivut.tarjonta.TarjontaComponent
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization.write

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent with TarjontaComponent =>

  val muistilistaService: MuistilistaService

  class MuistilistaService extends JsonFormats with Logging {

    def buildMail(muistiLista: Muistilista, url: StringBuffer): String = {
      url + "/muistilista/" + buildUlrEncodedOidString(muistiLista.oids)
      buildMessage(getKoulutuksetWithMuistiLista(muistiLista))
    }

    private def getKoulutuksetWithMuistiLista(muistiLista: Muistilista) = {
      val kieli = muistiLista.kieli
      val oids = muistiLista.oids.toList

      oids.map(k =>
        koulutusInformaatioService.koulutus(k, kieli) match {
          case Some(x) => x
          case _ => None
        }
      ).asInstanceOf[List[Koulutus]]
    }

    private def buildMessage(koulutukset: List[Koulutus]): String = {
      koulutukset.map(k =>
        s"Muistilista:\n ${k.name}, ${getHaku(k)}, ${getOpetusPiste(k)} - ${k.educationDegree}\n"+
        getSoraDescription(k))
        .mkString(",")
    }

    private def getOpetusPiste(koulutus: Koulutus): String = {
      koulutus.provider match {
        case Some(provider) => provider.name
        case _ => "Tuntematon opetuspiste"  //TODO: korvaa Translationilla
      }
    }

    private def getSoraDescription(koulutus: Koulutus): String = {
      koulutus.soraDescription match {
        case Some(desc) => desc
        case _ => "Tuntematon kuvaus"  //TODO: korvaa Translationilla
      }
    }

    private def getHaku(koulutus: Koulutus):String = {
      val lang = Language.parse(koulutus.teachingLanguages.head) match {
        case Some(lang) => lang
        case _ => Language.fi
      }

      tarjontaService.haku(koulutus.aoIdentifier, lang) match {
        case Some(haku) => haku.name
        case _ => "Tuntematon haku"  //TODO: korvaa Translationilla
      }
    }

    private def buildUlrEncodedOidString(oids: List[String]): String = {
      UrlValueCompressor.compress(write(oids))
    }

  }

}