package fi.vm.sade.omatsivut

import java.util
import java.util.Properties
import fi.vm.sade.omatsivut.AppConfig.AppConfig

import collection.JavaConversions._
import org.springframework.context.annotation._
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.{MapPropertySource, MutablePropertySources}

import scala.collection.JavaConversions

object OmatSivutSpringContext {
  val context = createApplicationContext(AppConfig.config)

  def check {}

  private def createApplicationContext(configuration: AppConfig): AnnotationConfigApplicationContext = {
    val appContext: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext
    println("Using spring configuration " + configuration.springConfiguration)
    appContext.getEnvironment.setActiveProfiles(configuration.springConfiguration.profile)
    customPropertiesHack(appContext, configuration)
    appContext.register(configuration.springConfiguration.getClass)
    appContext.refresh
    return appContext
  }

  def customPropertiesHack(appContext: AnnotationConfigApplicationContext, configuration: AppConfig) {
    val configurer: PropertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer()
    val sources: MutablePropertySources = new MutablePropertySources()

    val properties: Map[String, String] = configuration.springConfiguration.extraProps(configuration)

    sources.addFirst(new MapPropertySource("omatsivut custom props", mapAsJavaMap(properties)));
    configurer.setPropertySources(sources);
    appContext.addBeanFactoryPostProcessor(configurer)
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @Profile (Array ("dev") )
  @ImportResource (Array ("/META-INF/spring/logger-mock-context.xml") )
  object Dev extends OmatSivutConfiguration {
    val profile = "dev"
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @Profile (Array ("it") )
  @ImportResource (Array ("/META-INF/spring/logger-mock-context.xml") )
  object IT extends OmatSivutConfiguration {
    val profile = "it"
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @Profile (Array ("default") )
  @ImportResource (Array (  "file:///${user.home:''}/oph-configuration/security-context-backend.xml",
                            "/META-INF/spring/logger-context.xml") )
  object Default extends OmatSivutConfiguration {
    val profile = "default"
  }
}

trait OmatSivutConfiguration {
  def profile: String // <- should be able to get from annotation
  def extraProps(configuration: AppConfig) = configuration.settings.settingsReader.toMap
}