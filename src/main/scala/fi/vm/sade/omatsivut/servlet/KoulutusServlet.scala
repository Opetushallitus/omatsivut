package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.http.HttpClient
import fi.vm.sade.omatsivut.json.JsonFormats
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.swagger.{Swagger, SwaggerSupport}

class KoulutusServlet(implicit val swagger: Swagger, val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with SwaggerSupport {
  protected val applicationDescription = "Oppijan henkilökohtaisen palvelun REST API, jolla etsitään opetuspisteitä ja koulutuksia"

  before() {
    contentType = formats("json")
  }

  get("/opetuspisteet/:query") {
    // TODO: fixed urls
    val (responseCode, headersMap, resultString) = HttpClient.httpGet("https://testi.opintopolku.fi/lop/search/" + params("query"))
      .param("asId", params("asId"))
      .responseWithHeaders
    resultString
  }

  get("/koulutukset/:asId/:opetuspisteId") {
    // TODO: fixed urls
    val (responseCode, headersMap, resultString) = HttpClient.httpGet("https://testi.opintopolku.fi/ao/search/" + params("asId") + "/" + params("opetuspisteId"))
      .param("baseEducation", params("baseEducation"))
      .param("vocational", params("vocational"))
      .param("uiLang", params("uiLang"))
      .responseWithHeaders

    resultString
  }

}
