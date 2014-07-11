package fi.vm.sade.omatsivut

import java.io.{File, FileInputStream}
import java.util.Properties

import fi.vm.sade.omatsivut.AppConfig.AppConfig
import fi.vm.sade.omatsivut.mongo.EmbeddedMongo
import org.fusesource.scalate.support.FileTemplateSource
import org.specs2.mutable.Specification

class AppConfigSpec extends Specification{
  "Config with default profile" should {
    "Start up" in {
      EmbeddedMongo.withEmbeddedMongo {
        validateConfig(new AppConfig.Default() {
          override def configFiles = List(ConfigTemplateProcessor.createPropertyFileForTestingWithTemplate)
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

  "Config with dev-remote-mongo profile" should {
    "Start up" in {
      validateConfig(new AppConfig.DevWithRemoteMongo())
    }
  }
  def validateConfig(config: AppConfig) = {
    config.authenticationInfoService
    config.springContext.applicationDAO
    config.springContext.applicationSystemService
    config.springContext.mongoTemplate
    config.springContext.validator
    success
  }
}

object ConfigTemplateProcessor {
  import org.fusesource.scalate._
  import scala.collection.JavaConverters._

  def createPropertyFileForTestingWithTemplate = {
    val propertyFile: String = "target/omatsivut.fromtemplate.properties"
    val templateFile: String = "src/main/resources/oph-configuration/omatsivut.properties.template"
    val attributesFile: String = "src/main/resources/oph-configuration/example-vars.properties"
    ConfigTemplateProcessor.processTemplate(templateFile, attributesFile, propertyFile)
    propertyFile
  }

  def processTemplate(from: String, attributesFile: String, to: String) {
    val engine = new TemplateEngine
    val properties = new Properties
    properties.load(new FileInputStream(attributesFile))
    val templateSource: FileTemplateSource = new FileTemplateSource(new File(from), "template.mustache")
    val attributes: Map[String, Any] = properties.asScala.toMap.asInstanceOf[Map[String, Any]]

    val output: String =
      engine.layout(templateSource, attributes) + "\nmongodb.ensureIndex=false" // <- to make work with embedded mongo
    scala.tools.nsc.io.File(to).writeAll(output)
  }
}