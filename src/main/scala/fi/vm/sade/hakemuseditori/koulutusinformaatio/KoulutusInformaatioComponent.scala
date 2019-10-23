package fi.vm.sade.hakemuseditori.koulutusinformaatio

import fi.vm.sade.hakemuseditori.domain.Language.Language
import fi.vm.sade.hakemuseditori.fixtures.JsonFixtureMaps
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koulutusinformaatio.domain.{Koulutus, Opetuspiste}
import fi.vm.sade.hakemuseditori.lomake.LomakeRepositoryComponent
import fi.vm.sade.hakemuseditori.memoize.TTLOptionalMemoize
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.config.AppConfig.callerId
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
      parse(text, useBigDecimalForDouble = false).extract[Map[String, List[Opetuspiste]]].values.flatten.find(_.id == id)
    }

    def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = {
      val text = io.Source.fromInputStream(getClass.getResourceAsStream("/hakemuseditorimockdata/basketinfo.json")).mkString
      val allBasketItems = parse(text, useBigDecimalForDouble = false).extract[Option[List[KoulutusInformaatioBasketItem]]].getOrElse(List())
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

  class RemoteKoulutusService() extends KoulutusInformaatioService with JsonFormats with Logging {
    import org.json4s.jackson.JsonMethods._

    private def wrapAsOption[A](l: List[A]): Option[List[A]] = if (!l.isEmpty) Some(l) else None

    def opetuspisteet(asId: String, query: String, lang: Language): Option[List[Opetuspiste]] = {
      val (_, _, resultString) = DefaultHttpClient.httpGet(OphUrlProperties.url("koulutusinformaatio-app.lop.search", query))(callerId)
        .param("asId", asId)
        .param("lang", lang.toString)
        .param("ongoing", "true")
        .responseWithHeaders

      wrapAsOption(parse(resultString, useBigDecimalForDouble = false).extract[List[Opetuspiste]])
    }

    def opetuspiste(id: String, lang: Language): Option[Opetuspiste] = {
      DefaultHttpClient.httpGet(OphUrlProperties.url("koulutusinformaatio-app.lop", id))(callerId).response.flatMap { resultString =>
        parse(resultString, useBigDecimalForDouble = false).extract[Option[Opetuspiste]]
      }
    }

    def koulutukset(asId: String, opetuspisteId: String, baseEducation: Option[String], vocational: String, uiLang: Language): Option[List[Koulutus]] = {
      var request = DefaultHttpClient.httpGet(OphUrlProperties.url("koulutusinformaatio-app.ao.search", asId, opetuspisteId))(callerId)
        .param("uiLang", uiLang.toString)
        .param("ongoing", "true")
      if(!lomakeRepository.lomakeByOid(asId).map(_.baseEducationDoesNotRestrictApplicationOptions).getOrElse(false)) {
        if(baseEducation.isDefined) {
          request = request.param("baseEducation", baseEducation.get)
        }
        request = request.param("vocational", vocational)
      }
      val (_, _, resultString) = request.responseWithHeaders

      wrapAsOption(parse(resultString, useBigDecimalForDouble = false).extract[List[Koulutus]])
    }

    def koulutus(aoId: String, lang: Language): Option[Koulutus] = {
      val request = DefaultHttpClient.httpGet(OphUrlProperties.url("koulutusinformaatio-app.ao", aoId))(callerId)
        .param("lang", lang.toString)
        .param("uiLang", lang.toString)

      val (responseCode, headersMap, resultString) = request.responseWithHeaders()
      withWarnLogging{
        parse(resultString, useBigDecimalForDouble = false).extract[Option[Koulutus]]
      }(s"Parsing response from ${request.getUrl} failed:\n$resultString" , None)
    }

    def koulutusWithHaku(aoIds: List[String], lang: Language): Option[List[KoulutusInformaatioBasketItem]] = {
      var request = DefaultHttpClient.httpGet(OphUrlProperties.url("koulutusinformaatio-app.basketitems"))(callerId)
        .param("uiLang", lang.toString)
      aoIds.foreach(a =>  {
        request = request.param("aoId", a)
      })

      val (responseCode, headersMap, resultString) = request.responseWithHeaders
      withWarnLogging{
        parse(resultString, useBigDecimalForDouble = false).extract[Option[List[KoulutusInformaatioBasketItem]]]
      }(s"Parsing response from ${request.getUrl} failed:\n$resultString" , None)
    }
  }
}


