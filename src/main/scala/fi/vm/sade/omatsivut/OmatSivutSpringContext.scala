package fi.vm.sade.omatsivut

import org.springframework.context.annotation._
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

@Configuration
@ComponentScan (basePackages = Array ("fi.vm.sade.haku") )
@PropertySource (value = Array ("config/dev/haku.properties", "config/dev/ext.properties") )
@Profile (Array ("dev") )
@ImportResource (Array ("/META-INF/spring/logger-mock-context.xml") )
class OmatSivutSpringConfiguration {
  @Bean def enablePlaceholderReplacement: PropertySourcesPlaceholderConfigurer = {
    return new PropertySourcesPlaceholderConfigurer
  }
}

object OmatSivutSpringContext {
  val context = createApplicationContext

  def check {}

  private def createApplicationContext: AnnotationConfigApplicationContext = {
    val appContext: AnnotationConfigApplicationContext = new AnnotationConfigApplicationContext
    appContext.getEnvironment.setActiveProfiles("dev")
    appContext.register(classOf[OmatSivutSpringConfiguration])
    appContext.refresh
    return appContext
  }
}