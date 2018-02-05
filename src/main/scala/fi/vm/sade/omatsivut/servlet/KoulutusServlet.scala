package fi.vm.sade.omatsivut.servlet

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.hakemuseditori.koulutusinformaatio.{KoulutusInformaatioComponent, KoulutusInformaatioService}
import org.scalatra.json.JacksonJsonSupport

trait KoulutusServletContainer {
  this: KoulutusInformaatioComponent =>

  val koulutusInformaatioService: KoulutusInformaatioService

  class KoulutusServlet extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats {
    before() {
      contentType = formats("json")
    }

    get("/opetuspisteet/:query") {
      checkNotFound(koulutusInformaatioService.opetuspisteet(params("asId"), params("query"), getLangParam("lang")))
    }

    get("/koulutukset/:asId/:opetuspisteId") {
      checkNotFound(koulutusInformaatioService.koulutukset(params("asId"), params("opetuspisteId"), paramOption("baseEducation"), params("vocational"), getLangParam("uiLang")))
    }

    get("/koulutus/:aoId") {
      checkNotFound(koulutusInformaatioService.koulutus(params("aoId"), getLangParam("lang")))
    }

    def getLangParam(param: String): Language.Value = {
      paramOption(param).flatMap(Language.parse(_)).getOrElse(Language.fi)
    }
  }
}

