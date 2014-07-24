package fi.vm.sade.omatsivut

import java.io.{File, FileInputStream}
import java.util.Properties

import fi.vm.sade.omatsivut.AppConfig.{TemplatedProps, AppConfig}
import fi.vm.sade.omatsivut.mongo.EmbeddedMongo
import fi.vm.sade.omatsivut.security.AuthenticationInfoService
import org.specs2.mutable.Specification

class AppConfigSpec extends Specification{
  trait ExampleTemplatedProps extends TemplatedProps {
    def templateAttributesFile = "src/main/resources/oph-configuration/example-vars.yml"
  }
  "Config with default profile" should {
    "Start up" in {
      EmbeddedMongo.withEmbeddedMongo {
        validateConfig(new AppConfig.Default with ExampleTemplatedProps)
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

  "Config with dev-remote-mongo profile" should {
    "Start up" in {
      validateConfig(new AppConfig.DevWithRemoteMongo with ExampleTemplatedProps)
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

