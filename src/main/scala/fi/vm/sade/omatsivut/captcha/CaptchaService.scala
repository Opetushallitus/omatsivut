package fi.vm.sade.omatsivut.captcha

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.json.JsonFormats

trait CaptchaServiceComponent {

  def captchaService(): CaptchaService

  trait CaptchaService {
    def checkCaptcha(captcha: String): Boolean
  }

  class RemoteCaptchaService(appConfig: AppConfig) extends CaptchaService with JsonFormats {
    def checkCaptcha(captcha: String): Boolean = {
      if (appConfig.settings.recaptchaSecret.isEmpty) {
        true
      }
      else {
        false
      }
    }
  }
}
