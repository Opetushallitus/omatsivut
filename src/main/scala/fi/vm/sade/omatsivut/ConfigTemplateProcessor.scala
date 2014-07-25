package fi.vm.sade.omatsivut

import java.io.{File, FileInputStream}
import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.`type`.MapType
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.fusesource.scalate.TemplateEngine
import org.fusesource.scalate.support.FileTemplateSource

import scala.collection.JavaConverters._

object ConfigTemplateProcessor {
  def createPropertyFileForTestingWithTemplate(attributesFile: String) = {
    val propertyFile: String = "target/omatsivut.fromtemplate." + attributesFile.hashCode + ".properties"
    val templateFile: String = "src/main/resources/oph-configuration/omatsivut.properties.template"
    ConfigTemplateProcessor.processTemplate(templateFile, attributesFile, propertyFile)
    propertyFile
  }

  def processTemplate(from: String, attributesFile: String, to: String) {
    val mapper: ObjectMapper = new ObjectMapper(new YAMLFactory())
    val mapType: MapType = mapper.getTypeFactory.constructMapType(classOf[util.HashMap[String, String]], classOf[String], classOf[String])
    val rawValue = mapper.readValue(new FileInputStream(attributesFile), mapType).asInstanceOf[util.HashMap[String, String]]
    val attributes: Map[String, Any] = rawValue.asScala.toMap.asInstanceOf[Map[String, Any]]
    val engine = new TemplateEngine

    val templateSource: FileTemplateSource = new FileTemplateSource(new File(from), "template.mustache")

    val output: String =
      engine.layout(templateSource, attributes) + "\nmongodb.ensureIndex=false" // <- to make work with embedded mongo
    scala.tools.nsc.io.File(to).writeAll(output)
  }
}
