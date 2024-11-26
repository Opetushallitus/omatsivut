package fi.vm.sade.omatsivut.util

import java.io.{File, StringReader}
import java.net.URL
import java.util.Properties

import com.typesafe.config.ConfigFactory

object ConfigTemplateProcessor {
  def createSettings[T <: ApplicationSettings](projectName: String, attributesFile: String)(implicit applicationSettingsParser: ApplicationSettingsParser[T]): T = {
    val templateURL: URL = new URL(
      null,
      "classpath:oph-configuration/" + projectName + ".properties.template",
      new ClassPathUrlHandler(getClass.getClassLoader))
    val attributesURL = new File(attributesFile).toURI.toURL

    val templatedData = JinjaTemplateProcessor.processJinjaWithYamlAttributes(templateURL, attributesURL) + "\nmongodb.ensureIndex=false" // <- to make work with embedded mongo
    parseTemplatedData(templatedData)
  }

  def createSettings[T <: ApplicationSettings](template: URL, attributes: URL)(implicit applicationSettingsParser: ApplicationSettingsParser[T]): T = {
    val templatedData: String = JinjaTemplateProcessor.processJinjaWithYamlAttributes(template, attributes) + "\nmongodb.ensureIndex=false" // <- to make work with embedded mongo
    parseTemplatedData(templatedData)
  }

  def parseTemplatedData[T <: ApplicationSettings](templatedData: String)(implicit applicationSettingsParser: ApplicationSettingsParser[T]): T = {
    val properties = new Properties()
    properties.load(new StringReader(templatedData))
    applicationSettingsParser.parse(ConfigFactory.load(ConfigFactory.parseProperties(properties)))
  }
}

