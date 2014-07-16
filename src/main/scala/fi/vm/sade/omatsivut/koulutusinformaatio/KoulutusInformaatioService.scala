package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.json.JsonFormats
import org.json4s.jackson.JsonMethods._

object KoulutusInformaatioService extends JsonFormats{
  // TODO: fixed urls

  def opetuspisteet(asId: String, query: String): List[Opetuspiste] = {
    val (responseCode, headersMap, resultString) = HttpClient.httpGet("https://testi.opintopolku.fi/lop/search/" + query)
      .param("asId", asId)
      .responseWithHeaders

    parse(resultString).extract[List[Opetuspiste]]
  }


  def koulutukset(asId: String, opetuspisteId: String, baseEducation: String, vocational: String, uiLang: String): List[Koulutus] = {
    val (responseCode, headersMap, resultString) = HttpClient.httpGet("https://testi.opintopolku.fi/ao/search/" + asId + "/" + opetuspisteId)
      .param("baseEducation", baseEducation)
      .param("vocational", vocational)
      .param("uiLang", uiLang)
      .responseWithHeaders

    parse(resultString).extract[List[Koulutus]]
  }
}
