package fi.vm.sade.omatsivut.config

import fi.vm.sade.omatsivut.config.AppConfig.AppConfig
import fi.vm.sade.omatsivut.mongo.OmatSivutMongoConfiguration
import fi.vm.sade.omatsivut.util.Logging
import org.springframework.context.annotation._
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.{MapPropertySource, MutablePropertySources}

import scala.collection.JavaConversions._

object OmatSivutSpringContext extends Logging {
  def check {}

  def createApplicationContext(configuration: AppConfig): AnnotationConfigApplicationContext = {
    val appContext: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext
    logger.info("Using spring configuration " + configuration.springConfiguration)
    appContext.getEnvironment.setActiveProfiles(configuration.springConfiguration.profile)
    customPropertiesHack(appContext, configuration)
    appContext.register(configuration.springConfiguration.getClass)
    appContext.refresh()
    appContext
  }

  private def customPropertiesHack(appContext: AnnotationConfigApplicationContext, configuration: AppConfig) {
    val configurer: PropertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer()
    val sources: MutablePropertySources = new MutablePropertySources()

    sources.addFirst(new MapPropertySource("omatsivut custom props", mapAsJavaMap(configuration.settings.toProperties)))
    configurer.setPropertySources(sources)
    appContext.addBeanFactoryPostProcessor(configurer)
  }

  @Configuration
  @Import(Array(classOf[OmatSivutMongoConfiguration]))
  class Dev extends OmatSivutConfiguration {
    val profile = "dev"

    @Bean def hakumaksuService = null

    @Bean def sendMailService = null

  }

  @Configuration
  @ComponentScan(basePackages = Array(
    "fi.vm.sade.security"
  ))
  @Import(Array(classOf[OmatSivutMongoConfiguration]))
  class Default extends OmatSivutConfiguration {
    val profile = "default"


  }

  class Cloud extends Default {
    override val profile = "cloud"
  }

}

trait OmatSivutConfiguration {
  def profile: String // <- should be able to get from annotation
}
