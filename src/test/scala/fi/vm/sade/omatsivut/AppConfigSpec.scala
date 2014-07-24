package fi.vm.sade.omatsivut

import fi.vm.sade.omatsivut.AppConfig.{AppConfig, TemplatedProps}
import fi.vm.sade.omatsivut.mongo.EmbeddedMongo
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import org.specs2.mutable.Specification

class AppConfigSpec extends Specification {
  "Config with default profile" should {
    "Start up" in {
      EmbeddedMongo.withEmbeddedMongo {
        validateConfig(new AppConfig with TemplatedProps {
          def springConfiguration = new OmatSivutSpringContext.Default()
          def templateAttributesFile = "src/main/resources/oph-configuration/example-vars.yml"
        })
      }
    }
  }

  "Config with it profile" should {
    "Start up" in {
      validateConfig(new AppConfig.IT())
    }
  }

  "Config with dev profile" should {
    "Start up" in {
      validateConfig(new AppConfig.Dev())
    }
  }

  def validateConfig(config: AppConfig) = {
    AuthenticationInfoService.apply(config)
    config.springContext.applicationDAO
    config.springContext.applicationSystemService
    config.springContext.mongoTemplate
    config.springContext.validator
    success
  }
}

