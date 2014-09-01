package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.config.AppConfig.{AppConfig, ExampleTemplatedProps}
import fi.vm.sade.omatsivut.mongo.EmbeddedMongo
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import org.specs2.mutable.Specification

class AppConfigSpec extends Specification {
  "Config with default profile" should {
    "Start up" in {
      EmbeddedMongo.withEmbeddedMongo {
        validateConfig(new AppConfig with ExampleTemplatedProps {
          def springConfiguration = new OmatSivutSpringContext.Default()
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

