package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.{ITSetup, OmatsivutDbTools, config}
import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.utils.config.ConfigTemplateProcessor
import org.junit.runner.RunWith
import org.specs2.matcher.PathMatchers
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AppConfigSpec extends Specification with ITSetup with OmatsivutDbTools {
  sequential

  step(appConfig.onStart)
  step(deleteAllSessions())

  "Config with default profile" should {
    "Start up" in {
      validateConfig(new AppConfig.IT {
        override def springConfiguration = new OmatSivutSpringContext.Default()
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

  "common.properties.template" should {
    "must exist with that name for deployment process" in {
      "src/main/resources/oph-configuration/common.properties.template" must PathMatchers.beAnExistingPath
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

