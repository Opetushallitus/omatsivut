package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.config.AppConfig.{AppConfig, ExampleTemplatedProps}
import fi.vm.sade.omatsivut.mongo.EmbeddedMongo
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
    val componentRegistry = new ComponentRegistry(config)
    componentRegistry.springContext.applicationDAO
    componentRegistry.springContext.applicationSystemService
    componentRegistry.springContext.mongoTemplate
    componentRegistry.springContext.validator
    success
  }
}

