package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.AppConfig.{AppConfig, StubbedExternalDeps}
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.json.JsonFormats
import scalaj.http.Http
import fi.vm.sade.omatsivut.http.DefaultHttpClient
import fi.vm.sade.omatsivut.Logging
import fi.vm.sade.omatsivut.domain.Language

trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String): List[Opetuspiste]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: String): List[Koulutus]
  def koulutus(aoId: String): Option[Koulutus]

  def liitepyynto(aoId: String)(implicit lang: Language.Language): Liitepyynto = {
    val liitepyynto = koulutus(aoId).map(koulutus => Liitepyynto(
              aoId,
              koulutus.provider.map(_.name),
              getAttachmentAddress(koulutus),
              koulutus.attachmentDeliveryDeadline
        ))
    liitepyynto.getOrElse(Liitepyynto(aoId))
  }

  private def getAttachmentAddress(info: Koulutus): Option[Address] = {
    if(info.attachmentDeliveryAddress.isDefined) {
      info.attachmentDeliveryAddress
    }
    else {
      info.provider flatMap(_.applicationOffice) flatMap(_.postalAddress )
    }
  }
}

object KoulutusInformaatioService {
  def apply(implicit appConfig: AppConfig): KoulutusInformaatioService = appConfig match {
    case x: StubbedExternalDeps => new KoulutusInformaatioService {
      def opetuspisteet(asId: String, query: String) = {
        JsonFixtureMaps.findByKey[List[Opetuspiste]]("/mockdata/opetuspisteet.json", query.substring(0, 1).toLowerCase).getOrElse(List())
      }
      def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: String) = {
        JsonFixtureMaps.findByKey[List[Koulutus]]("/mockdata/koulutukset.json", opetuspisteId).getOrElse(List())
      }
      def koulutus(aoId: String) = {
        JsonFixtureMaps.findByFieldValue[List[Koulutus]]("/mockdata/koulutukset.json", "id", aoId).getOrElse(List()).headOption
      }
    }
    case _ => RemoteKoulutusService()
  }
}

case class RemoteKoulutusService(implicit appConfig: AppConfig) extends KoulutusInformaatioService with JsonFormats with Logging {
  import org.json4s.jackson.JsonMethods._

  def opetuspisteet(asId: String, query: String): List[Opetuspiste] = {
    val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioLopUrl + "/search/" + Http.urlEncode(query, "UTF-8"))
      .param("asId", asId)
      .responseWithHeaders

    parse(resultString).extract[List[Opetuspiste]]
  }


  def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: String): List[Koulutus] = {
    var request = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioAoUrl + "/search/" + asId + "/" + opetuspisteId)
      .param("vocational", vocational)
      .param("uiLang", uiLang)
    if(baseEducation.isDefined) {
      request = request.param("baseEducation", baseEducation.get)
    }
    val (responseCode, headersMap, resultString) = request.responseWithHeaders

    parse(resultString).extract[List[Koulutus]]
  }

  def koulutus(aoId: String): Option[Koulutus] = {
    val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioAoUrl + "/" + aoId)
      .responseWithHeaders
    withWarnLogging{
      parse(resultString).extract[Option[Koulutus]]
    }("Parsing response failed:\n" + resultString, None)
  }

}