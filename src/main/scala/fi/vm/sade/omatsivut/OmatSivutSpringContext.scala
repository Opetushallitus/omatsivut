package fi.vm.sade.omatsivut

import org.springframework.context.annotation._
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

object OmatSivutSpringContext {
  val context = createApplicationContext

  def check {}

  private def createApplicationContext: AnnotationConfigApplicationContext = {
    val appContext: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext
    appContext.getEnvironment.setActiveProfiles(AppConfig.config.springProfile)
    appContext.register(classOf[Dev], classOf[IT], classOf[Default])
    appContext.refresh
    return appContext
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @PropertySource (value = Array ("config/dev/haku.properties",
                                  "config/dev/ext.properties") )
  @Profile (Array ("dev") )
  @ImportResource (Array ("/META-INF/spring/logger-mock-context.xml") )
  class Dev extends OmatSivutConfiguration {
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @PropertySource (value = Array ("config/it/haku.properties", "config/it/ext.properties") )
  @Profile (Array ("it") )
  @ImportResource (Array ("/META-INF/spring/logger-mock-context.xml") )
  class IT extends OmatSivutConfiguration {
  }

  @Configuration
  @ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
  @PropertySource (value = Array (  "file:///${user.home:''}/oph-configuration/haku.properties",
                                    "file:///${user.home:''}/oph-configuration/common.properties",
                                    "file:///${user.home:''}/oph-configuration/override.properties") )
  @Profile (Array ("default") )
  @ImportResource (Array (  "file:///${user.home:''}/oph-configuration/security-context-backend.xml",
                            "/META-INF/spring/logger-context.xml") )
  class Default extends OmatSivutConfiguration {
    @Bean override def enablePlaceholderReplacement: PropertySourcesPlaceholderConfigurer = {
      val configurer: PropertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer
      configurer.setIgnoreResourceNotFound(true)
      return configurer
    }
  }

  abstract class OmatSivutConfiguration {
    @Bean def enablePlaceholderReplacement: PropertySourcesPlaceholderConfigurer = {
      val configurer: PropertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer
      configurer.setIgnoreResourceNotFound(true)
      return configurer
    }
  }
}