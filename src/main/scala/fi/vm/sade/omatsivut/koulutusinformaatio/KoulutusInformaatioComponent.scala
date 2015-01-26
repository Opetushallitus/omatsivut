package fi.vm.sade.omatsivut.koulutusinformaatio

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.domain.Language.Language
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps
import fi.vm.sade.omatsivut.fixtures.JsonFixtureMaps._
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.koulutusinformaatio.domain.{Koulutus, Opetuspiste}
import fi.vm.sade.omatsivut.memoize.TTLOptionalMemoize
import fi.vm.sade.omatsivut.muistilista.{MuistilistaKoulutusInfo, KoulutusInformaatioBasketItem}
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods._
import scalaj.http.Http

trait KoulutusInformaatioComponent {
  val koulutusInformaatioService: KoulutusInformaatioService

  class StubbedKoulutusInformaatioService extends KoulutusInformaatioService {
    def opetuspisteet(asId: String, query: String, lang: Language) = {
      JsonFixtureMaps.findByKey[List[Opetuspiste]]("/mockdata/opetuspisteet.json", query.substring(0, 1).toLowerCase)
    }
    def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: Language) = {
      JsonFixtureMaps.findByKey[List[Koulutus]]("/mockdata/koulutukset.json", opetuspisteId)
    }
    def koulutus(aoId: String, lang: Language) = {
      JsonFixtureMaps.findByFieldValue[List[Koulutus]]("/mockdata/koulutukset.json", "id", aoId).getOrElse(List()).headOption
    }

    def opetuspiste(id: String, lang: Language) = {
      val text = io.Source.fromInputStream(getClass.getResourceAsStream("/mockdata/opetuspisteet.json")).mkString
      parse(text).extract[Map[String, List[Opetuspiste]]].values.flatten.find(_.id == id)
    }

    def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = {
      val text = io.Source.fromInputStream(getClass.getResourceAsStream("/mockdata/basketinfo.json")).mkString
      val allBasketItems = parse(text).extract[Option[List[KoulutusInformaatioBasketItem]]].getOrElse(List())
      Some(allBasketItems.filter( bi => bi.applicationOptions.filter( ao => aoIds.contains(ao.id)).nonEmpty))
    }
  }

  object CachedKoulutusInformaatioService {
    def apply(implicit appConfig: AppConfig): KoulutusInformaatioService = {
      val service = new RemoteKoulutusService()
      val cacheTimeSec = 60*15
      val opetuspisteMemo = TTLOptionalMemoize.memoize(service.opetuspiste _, "koulutusinformaatio opetuspiste", cacheTimeSec, 32)
      val opetuspisteetMemo = TTLOptionalMemoize.memoize(service.opetuspisteet _, "koulutusinformaatio opetuspisteet", cacheTimeSec, 32)
      val koulutusMemo = TTLOptionalMemoize.memoize(service.koulutus _, "koulutusinformaatio koulutus", cacheTimeSec, 32)
      val koulutuksetMemo = TTLOptionalMemoize.memoize(service.koulutukset _, "koulutusinformaatio koulutukset", cacheTimeSec, 32)
      val koulutusWithHakuMemo = TTLOptionalMemoize.memoize(service.koulutusWithHaku _, "koulutusinformaatio muistilistan koulutukset", cacheTimeSec, 32)

      new KoulutusInformaatioService {
        def opetuspisteet(asId: String, query: String, lang: Language): Option[List[Opetuspiste]] = opetuspisteetMemo(asId, query, lang)
        def koulutus(aoId: String, lang: Language): Option[Koulutus] = koulutusMemo(aoId, lang)
        def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: Language): Option[List[Koulutus]] = koulutuksetMemo(asId, opetuspisteId, baseEducation, vocational, uiLang)
        def opetuspiste(id: String, lang: Language) = opetuspisteMemo(id, lang)
        def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = koulutusWithHakuMemo(aoIds, lang)
      }
    }
  }

  class RemoteKoulutusService(implicit appConfig: AppConfig) extends KoulutusInformaatioService with JsonFormats with Logging {
    import org.json4s.jackson.JsonMethods._

    private def wrapAsOption[A](l: List[A]): Option[List[A]] = if (!l.isEmpty) Some(l) else None

    def opetuspisteet(asId: String, query: String, lang: Language): Option[List[Opetuspiste]] = {
      val (_, _, resultString) = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioLopUrl + "/search/" + Http.urlEncode(query, "UTF-8"))
        .param("asId", asId)
        .param("lang", lang.toString)
        .responseWithHeaders

      wrapAsOption(parse(resultString).extract[List[Opetuspiste]])
    }

    def opetuspiste(id: String, lang: Language): Option[Opetuspiste] = {
      DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioLopUrl + "/" + id).response.flatMap { resultString =>
        parse(resultString).extract[Option[Opetuspiste]]
      }
    }

    def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: Language): Option[List[Koulutus]] = {
      var request = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioAoUrl + "/search/" + asId + "/" + opetuspisteId)
        .param("vocational", vocational)
        .param("uiLang", uiLang.toString)
      if(baseEducation.isDefined) {
        request = request.param("baseEducation", baseEducation.get)
      }
      val (_, _, resultString) = request.responseWithHeaders

      wrapAsOption(parse(resultString).extract[List[Koulutus]])
    }

    def koulutus(aoId: String, lang: Language): Option[Koulutus] = {
      val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(appConfig.settings.koulutusinformaatioAoUrl + "/" + aoId)
        .param("lang", lang.toString)
        .param("uiLang", lang.toString)
        .responseWithHeaders
      withWarnLogging{
        parse(resultString).extract[Option[Koulutus]]
      }("Parsing response failed:\n" + resultString, None)
    }

    def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = {
      val params = aoIds.map(a => "&aoId="+a).mkString

      val url: String = appConfig.settings.koulutusinformaationBIUrl + "?uiLang=" + lang + params
      logger.info("url="+url)
      val (responseCode, headersMap, resultString) = DefaultHttpClient.httpGet(url)
        .responseWithHeaders
      withWarnLogging{
        parse(resultString).extract[Option[List[KoulutusInformaatioBasketItem]]]
      }("Parsing response failed:\n" + resultString, None)
    }
  }
}

