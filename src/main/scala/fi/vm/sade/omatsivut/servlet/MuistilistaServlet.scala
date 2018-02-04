package fi.vm.sade.omatsivut.servlet

import java.net.URLEncoder

import fi.vm.sade.hakemuseditori.domain.Language
import fi.vm.sade.hakemuseditori.http.UrlValueCompressor
import fi.vm.sade.utils.captcha.CaptchaServiceComponent
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.hakemuseditori.json.JsonFormats
import fi.vm.sade.omatsivut.OphUrlProperties
import fi.vm.sade.omatsivut.muistilista.{Muistilista, MuistilistaServiceComponent}
import fi.vm.sade.utils.slf4j.Logging
import org.json4s.MappingException
import org.json4s.jackson.Serialization
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.{Cookie, CookieOptions}

trait MuistilistaServletContainer {

  this: MuistilistaServiceComponent with CaptchaServiceComponent =>

  class MuistilistaServlet(val appConfig: AppConfig) extends OmatSivutServletBase with JacksonJsonSupport with JsonFormats with Logging {

    get("/:compressedMuistilista") {
      val compressedMuistilista = params("compressedMuistilista")
      val asString = UrlValueCompressor.decompress(compressedMuistilista)
      response.addCookie(Cookie("basket", URLEncoder.encode(asString, "UTF-8"))(CookieOptions(path = "/")))
      response.redirect(OphUrlProperties.url("koulutusinformaatio-app.muistilista"))
    }

    post() {
      try {
        val muistiLista = Serialization.read[Muistilista](request.body)
        if(!captchaService.checkCaptcha(muistiLista.captcha)) {
          logger.warn("muistiLista with invalid captcha:" + request.body)
          response.setStatus(403)
          "403 Forbidden"
        }
        else if (muistiLista.otsikko.isEmpty || List(muistiLista.vastaanottaja, muistiLista.koids).exists(_.isEmpty)) {
          logger.warn("muistiLista malformed:" + request.body)
          response.setStatus(400)
          "400 Bad Request"
        } else {

          var baseUrl = appConfig.settings.oppijaBaseUrlFi
          if(Language.en.equals(muistiLista.kieli)) {
            baseUrl = appConfig.settings.oppijaBaseUrlEn
          } else if(Language.sv.equals(muistiLista.kieli)) {
            baseUrl = appConfig.settings.oppijaBaseUrlSv
          }

          var muistilistaUrl = baseUrl + request.getRequestURI
          muistilistaService(muistiLista.kieli).sendMail(muistiLista, muistilistaUrl)
        }
      } catch {
        case e: MappingException =>
          logger.warn("Invalid input: " + e.getMessage + " (request data: " + request.body + ")")
          response.setStatus(400)
          "400 Bad Request"
      }
    }

  }

}
