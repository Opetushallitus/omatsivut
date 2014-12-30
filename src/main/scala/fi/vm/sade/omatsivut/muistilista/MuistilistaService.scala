package fi.vm.sade.omatsivut.muistilista

import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.domain.Koulutus
import fi.vm.sade.omatsivut.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.jackson.Serialization.write
import org.scalatra.json.JacksonJsonSupport

trait MuistilistaServiceComponent {
  this: KoulutusInformaatioComponent =>

  val muistilistaService: MuistilistaService

  class MuistilistaService extends JsonFormats with Logging {

    def buildMail(muistiLista: Muistilista, url: StringBuffer): String = {
      url + "/muistilista/" + buildUlrEncodedOidString(muistiLista.oids)
      buildText(muistiLista).mkString
    }

    //TODO: write test
    private def buildText(muistiLista: Muistilista) = {
      val kieli = muistiLista.kieli
      val oids = muistiLista.oids.toList

      oids.map(k =>
        koulutusInformaatioService.koulutus(k, kieli) match {
          case Some(x) => x.toString
          case _ => ""
        }
      )
    }

    private def buildUlrEncodedOidString(oids: List[String]): String = {
      UrlValueCompressor.compress(write(oids))
    }

  }

}