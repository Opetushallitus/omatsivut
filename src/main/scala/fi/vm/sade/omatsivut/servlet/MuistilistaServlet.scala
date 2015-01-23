package fi.vm.sade.omatsivut.servlet

import java.net.URLEncoder

import fi.vm.sade.omatsivut.captcha.CaptchaServiceComponent
import fi.vm.sade.omatsivut.config.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.http.UrlValueCompressor
import fi.vm.sade.omatsivut.json.JsonFormats
import fi.vm.sade.omatsivut.muistilista.{Muistilista, MuistilistaServiceComponent}
import fi.vm.sade.utils.slf4j.Logging
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
      response.redirect(appConfig.settings.muistilistaUrl)
    }

    post() {
      val muistiLista = Serialization.read[Muistilista](request.body)
      if(!captchaService.checkCaptcha(muistiLista.captcha)) {
        logger.warn("muistiLista with invalid captcha:" + request.body)
        response.setStatus(403)
        "403 Forbidden"
      }
      if (muistiLista.otsikko.isEmpty || List(muistiLista.vastaanottaja, muistiLista.koids).exists(_.isEmpty)) {
        logger.warn("muistiLista malformed:" + request.body)
        response.setStatus(400)
        "400 Bad Request"
      } else {
        muistilistaService(muistiLista.kieli).sendMail(muistiLista, request.getRequestURL)
      }
    }

  }

}
