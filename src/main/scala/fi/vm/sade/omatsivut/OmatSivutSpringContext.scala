package fi.vm.sade.omatsivut

import fi.vm.sade.haku.oppija.configuration.MongoConfiguration
import fi.vm.sade.haku.oppija.hakemus.it.dao.ApplicationDAO
import fi.vm.sade.haku.oppija.hakemus.service.HakuPermissionService
import fi.vm.sade.haku.oppija.hakemus.service.impl.HakuPermissionServiceMockImpl
import fi.vm.sade.haku.oppija.lomake.service.ApplicationSystemService
import fi.vm.sade.haku.oppija.lomake.validation.ElementTreeValidator
import fi.vm.sade.haku.virkailija.authentication.AuthenticationService
import fi.vm.sade.haku.virkailija.authentication.impl.AuthenticationServiceMockImpl
import fi.vm.sade.omatsivut.AppConfig.AppConfig
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation._
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.core.env.{MapPropertySource, MutablePropertySources}
import org.springframework.data.mongodb.core.MongoTemplate

import scala.collection.JavaConversions._

class OmatSivutSpringContext(context: ApplicationContext) {
  def applicationSystemService = context.getBean(classOf[ApplicationSystemService])

  def applicationDAO = context.getBean(classOf[ApplicationDAO])

  def mongoTemplate = context.getBean(classOf[MongoTemplate])

  def validator = context.getBean(classOf[ElementTreeValidator])
}

object OmatSivutSpringContext {
  val context = new OmatSivutSpringContext(createApplicationContext(AppConfig.config))

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

    val properties: Map[String, String] = configuration.settings.toProperties

    sources.addFirst(new MapPropertySource("omatsivut custom props", mapAsJavaMap(properties)));
    configurer.setPropertySources(sources);
    appContext.addBeanFactoryPostProcessor(configurer)
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @ImportResource (Array ("/META-INF/spring/logger-mock-context.xml") )
  object Dev extends OmatSivutConfiguration {
    val profile = "dev"
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @ImportResource (Array ("/META-INF/spring/logger-mock-context.xml") )
  object IT extends OmatSivutConfiguration {
    val profile = "dev"
  }

  @Configuration
  @ComponentScan (basePackages = Array (
    "fi.vm.sade.haku.oppija.lomake",
    "fi.vm.sade.haku.oppija.repository",
    "fi.vm.sade.haku.oppija.hakemus.it.dao",
    "fi.vm.sade.haku.oppija.hakemus.converter",
    "fi.vm.sade.haku.oppija.common.koulutusinformaatio") )
  @ImportResource (Array (  "/META-INF/spring/logger-mock-context.xml") )
  @Import(Array(classOf[MongoConfiguration]))
  object Default extends OmatSivutConfiguration {
    val profile = "default"

    @Bean def authenticationService: AuthenticationService = new AuthenticationServiceMockImpl
    @Bean def hakuPermissionService: HakuPermissionService = new HakuPermissionServiceMockImpl
  }
}

trait OmatSivutConfiguration {
  def profile: String // <- should be able to get from annotation
}