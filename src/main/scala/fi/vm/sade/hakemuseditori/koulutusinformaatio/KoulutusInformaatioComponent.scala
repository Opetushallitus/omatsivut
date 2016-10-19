package fi.vm.sade.hakemuseditori.koulutusinformaatio

import java.net.URLEncoder

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.fixtures.JsonFixtureMaps
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koulutusinformaatio.domain.{Koulutus, Opetuspiste}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.memoize.TTLOptionalMemoize
import fi.vm.sade.utils.http.DefaultHttpClient
import fi.vm.sade.utils.slf4j.Logging
import org.json4s._
import org.json4s.jackson.JsonMethods._

trait KoulutusInformaatioComponent {
  this: LomakeRepositoryComponent =>
  
  val koulutusInformaatioService: KoulutusInformaatioService

  class StubbedKoulutusInformaatioService extends KoulutusInformaatioService with JsonFormats {
    def opetuspisteet(asId: String, query: String, lang: Language) = {
      JsonFixtureMaps.findByKey[List[Opetuspiste]]("/hakemuseditorimockdata/opetuspisteet.json", query.substring(0, 1).toLowerCase)
    }
    def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: Language) = {
      JsonFixtureMaps.findByKey[List[Koulutus]]("/hakemuseditorimockdata/koulutukset.json", opetuspisteId)
    }
    def koulutus(aoId: String, lang: Language): Option[Koulutus] = {
      JsonFixtureMaps.findByFieldValue[List[Koulutus]]("/hakemuseditorimockdata/koulutukset.json", "id", aoId).getOrElse(List()).headOption
    }

    def opetuspiste(id: String, lang: Language) = {
      val text = io.Source.fromInputStream(getClass.getResourceAsStream("/hakemuseditorimockdata/opetuspisteet.json")).mkString
      parse(text).extract[Map[String, List[Opetuspiste]]].values.flatten.find(_.id == id)
    }

    def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = {
      val text = io.Source.fromInputStream(getClass.getResourceAsStream("/hakemuseditorimockdata/basketinfo.json")).mkString
      val allBasketItems = parse(text).extract[Option[List[KoulutusInformaatioBasketItem]]].getOrElse(List())
      Some(allBasketItems.filter( bi => bi.applicationOptions.filter( ao => aoIds.contains(ao.id)).nonEmpty))
    }
  }

  object CachedKoulutusInformaatioService {
    def apply(service: KoulutusInformaatioService): KoulutusInformaatioService = {
      val cacheTimeSec = 60*15
      val opetuspisteMemo = TTLOptionalMemoize.memoize(service.opetuspiste _, "koulutusinformaatio opetuspiste", cacheTimeSec, 1024)
      val opetuspisteetMemo = TTLOptionalMemoize.memoize(service.opetuspisteet _, "koulutusinformaatio opetuspisteet", cacheTimeSec, 1024)
      val koulutusMemo = TTLOptionalMemoize.memoize(service.koulutus _, "koulutusinformaatio koulutus", cacheTimeSec, 1024)
      val koulutuksetMemo = TTLOptionalMemoize.memoize(service.koulutukset _, "koulutusinformaatio koulutukset", cacheTimeSec, 1024)
      val koulutusWithHakuMemo = TTLOptionalMemoize.memoize(service.koulutusWithHaku _, "koulutusinformaatio muistilistan koulutukset", cacheTimeSec, 1024)

      new KoulutusInformaatioService {
        def opetuspisteet(asId: String, query: String, lang: Language): Option[List[Opetuspiste]] = opetuspisteetMemo(asId, query, lang)
        def koulutus(aoId: String, lang: Language): Option[Koulutus] = koulutusMemo(aoId, lang)
        def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: Language): Option[List[Koulutus]] = koulutuksetMemo(asId, opetuspisteId, baseEducation, vocational, uiLang)
        def opetuspiste(id: String, lang: Language) = opetuspisteMemo(id, lang)
        def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = koulutusWithHakuMemo(aoIds, lang)
      }
    }
  }

  class RemoteKoulutusService(koulutusinformaatioAoUrl: String, koulutusinformaationBIUrl: String, koulutusinformaatioLopUrl: String) extends KoulutusInformaatioService with JsonFormats with Logging {
    import org.json4s.jackson.JsonMethods._

    private def wrapAsOption[A](l: List[A]): Option[List[A]] = if (!l.isEmpty) Some(l) else None

    def opetuspisteet(asId: String, query: String, lang: Language): Option[List[Opetuspiste]] = {
      val (_, _, resultString) = DefaultHttpClient.httpGet(koulutusinformaatioLopUrl + "/search/" + URLEncoder.encode(query, "UTF-8"))
        .param("asId", asId)
        .param("lang", lang.toString)
        .param("ongoing", "true")
        .responseWithHeaders

      wrapAsOption(parse(resultString).extract[List[Opetuspiste]])
    }

    def opetuspiste(id: String, lang: Language): Option[Opetuspiste] = {
      DefaultHttpClient.httpGet(koulutusinformaatioLopUrl + "/" + id).response.flatMap { resultString =>
        parse(resultString).extract[Option[Opetuspiste]]
      }
    }

    def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: Language): Option[List[Koulutus]] = {
      var request = DefaultHttpClient.httpGet(koulutusinformaatioAoUrl + "/search/" + asId + "/" + opetuspisteId)
        .param("uiLang", uiLang.toString)
        .param("ongoing", "true")
      if(!lomakeRepository.lomakeByOid(asId).map(_.baseEducationDoesNotRestrictApplicationOptions).getOrElse(false)) {
        if(baseEducation.isDefined) {
          request = request.param("baseEducation", baseEducation.get)
        }
        request = request.param("vocational", vocational)
      }
      val (_, _, resultString) = request.responseWithHeaders

      wrapAsOption(parse(resultString).extract[List[Koulutus]])
    }

    def koulutus(aoId: String, lang: Language): Option[Koulutus] = {
      val request = DefaultHttpClient.httpGet(koulutusinformaatioAoUrl + "/" + aoId)
        .param("lang", lang.toString)
        .param("uiLang", lang.toString)

      val (responseCode, headersMap, resultString) = request.responseWithHeaders()
      withWarnLogging{
        parse(resultString).extract[Option[Koulutus]]
      }(s"Parsing response from ${request.getUrl} failed:\n$resultString" , None)
    }

    def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = {
      var request = DefaultHttpClient.httpGet(koulutusinformaationBIUrl)
        .param("uiLang", lang.toString)
      aoIds.foreach(a =>  {
        request = request.param("aoId", a)
      })

      val (responseCode, headersMap, resultString) = request.responseWithHeaders
      withWarnLogging{
        parse(resultString).extract[Option[List[KoulutusInformaatioBasketItem]]]
      }(s"Parsing response from ${request.getUrl} failed:\n$resultString" , None)
    }
  }
}


