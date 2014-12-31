package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.KoulutusInformaatioComponent
import fi.vm.sade.omatsivut.koulutusinformaatio.domain.Koulutus
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization.write

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent =>

  val muistilistaService: MuistilistaService

  class MuistilistaService extends JsonFormats with Logging {

    def buildMail(muistiLista: Muistilista, url: StringBuffer): String = {
      url + "/muistilista/" + buildUlrEncodedOidString(muistiLista.oids)
      buildText(muistiLista)
    }

    private def buildText(muistiLista: Muistilista): String = {
      val kieli = muistiLista.kieli
      val oids = muistiLista.oids.toList

      val koulutus = oids.map(k =>
        koulutusInformaatioService.koulutus(k, kieli) match {
          case Some(x) => x
          case _ => None
        }
      ).asInstanceOf[List[Koulutus]]

      koulutus.map(ki => ki.name).mkString(", ")
    }

    private def buildUlrEncodedOidString(oids: List[String]): String = {
      UrlValueCompressor.compress(write(oids))
    }

  }

}