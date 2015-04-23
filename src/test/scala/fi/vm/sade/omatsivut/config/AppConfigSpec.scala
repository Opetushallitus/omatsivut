package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.config.AppConfig.{EmbbeddedMongo, AppConfig}
import org.junit.runner.RunWith
import org.specs2.matcher.PathMatchers
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AppConfigSpec extends Specification {
  "Config with default profile" should {
    "Start up" in {
      validateConfig(new EmbbeddedMongo {
        def springConfiguration = new OmatSivutSpringContext.Default()
      })
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

  "omatsivut.properties.template" should {
    "must exist with that name for deployment process" in {
      "src/main/resources/oph-configuration/omatsivut.properties.template" must PathMatchers.beAnExistingPath
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

