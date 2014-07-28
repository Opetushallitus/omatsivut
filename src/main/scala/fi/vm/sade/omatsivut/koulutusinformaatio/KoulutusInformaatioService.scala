package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.AppConfig.{AppConfig, StubbedExternalDeps}
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.json.JsonFormats
import scalaj.http.Http
import fi.vm.sade.omatsivut.http.DefaultHttpClient


trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String): List[Opetuspiste]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: String, vocational: String, uiLang: String): List[Koulutus]
}

object KoulutusInformaatioService {
  def apply(implicit appConfig: AppConfig): KoulutusInformaatioService = appConfig match {
    case x: StubbedExternalDeps => new KoulutusInformaatioService {
      def opetuspisteet(asId: String, query: String) = {
        JsonFixtureMaps.find[List[Opetuspiste]]("/mockdata/opetuspisteet.json", query.substring(0, 1).toLowerCase)
      }
      def koulutukset(asId: String, opetuspisteId: String, baseEducation: String, vocational: String, uiLang: String) = {
        JsonFixtureMaps.find[List[Koulutus]]("/mockdata/koulutukset.json", opetuspisteId)
      }
    }
    case _ => RemoteKoulutusService()
  }
}

case class RemoteKoulutusService(implicit appConfig: AppConfig) extends KoulutusInformaatioService with JsonFormats {
  import fi.vm.sade.omatsivut.http.HttpClient
  import org.json4s.jackson.JsonMethods._

  def opetuspisteet(asId: String, query: String): List[Opetuspiste] = {
    val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioLopUrl + "/search/" + Http.urlEncode(query, "UTF-8"))
      .param("asId", asId)
      .responseWithHeaders

    parse(resultString).extract[List[Opetuspiste]]
  }


  def koulutukset(asId: String, opetuspisteId: String, baseEducation: String, vocational: String, uiLang: String): List[Koulutus] = {
    val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioAoUrl + "/search/" + asId + "/" + opetuspisteId)
      .param("baseEducation", baseEducation)
      .param("vocational", vocational)
      .param("uiLang", uiLang)
      .responseWithHeaders

    parse(resultString).extract[List[Koulutus]]
  }
}