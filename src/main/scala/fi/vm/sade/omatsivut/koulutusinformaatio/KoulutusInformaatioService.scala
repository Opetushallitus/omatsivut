package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.AppConfig.{AppConfig, StubbedExternalDeps}
import fi.vm.sade.omatsivut.json.JsonFormats


trait KoulutusInformaatioService {
  def opetuspisteet(asId: String, query: String): List[Opetuspiste]
  def koulutukset(asId: String, opetuspisteId: String, baseEducation: String, vocational: String, uiLang: String): List[Koulutus]
}

object KoulutusInformaatioService {
  def apply(implicit appConfig: AppConfig): KoulutusInformaatioService = appConfig match {
    case x: StubbedExternalDeps => new KoulutusInformaatioService {
      def opetuspisteet(asId: String, query: String) = {
        JsonFixtureMaps.find[Opetuspiste]("/mockdata/opetuspisteet.json", query.substring(0, 1).toLowerCase)
      }
      def koulutukset(asId: String, opetuspisteId: String, baseEducation: String, vocational: String, uiLang: String) = {
        JsonFixtureMaps.find[Koulutus]("/mockdata/koulutukset.json", opetuspisteId)
      }
    }
    case _ => RemoteKoulutusService()
  }
}

object JsonFixtureMaps extends JsonFormats {
  import org.json4s._
  import org.json4s.jackson.JsonMethods._
  def find[T](dataFile: String, key: String)(implicit mf: Manifest[T]): List[T] = {
    val text = io.Source.fromInputStream(getClass.getResourceAsStream(dataFile)).mkString
    val parsed: JValue = parse(text).asInstanceOf[JObject]
    val found = parsed \ (key)
    found.extract[List[T]]
  }
}

case class RemoteKoulutusService(implicit appConfig: AppConfig) extends KoulutusInformaatioService with JsonFormats {
  import fi.vm.sade.omatsivut.http.HttpClient
  import org.json4s.jackson.JsonMethods._

  def opetuspisteet(asId: String, query: String): List[Opetuspiste] = {
    val (responseCode, headersMap, resultString) = HttpClient.httpGet(appConfig.settings.koulutusinformaatioLopUrl + "/search/" + query)
      .param("asId", asId)
      .responseWithHeaders

    parse(resultString).extract[List[Opetuspiste]]
  }


  def koulutukset(asId: String, opetuspisteId: String, baseEducation: String, vocational: String, uiLang: String): List[Koulutus] = {
    // TODO: fixed url
    val (responseCode, headersMap, resultString) = HttpClient.httpGet(appConfig.settings.koulutusinformaatioAoUrl + "/search/" + asId + "/" + opetuspisteId)
      .param("baseEducation", baseEducation)
      .param("vocational", vocational)
      .param("uiLang", uiLang)
      .responseWithHeaders

    parse(resultString).extract[List[Koulutus]]
  }
}