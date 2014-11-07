package fi.vm.sade.omatsivut.config

import java.util.concurrent.TimeUnit

import com.google.common.cache.{Weigher, CacheBuilder}
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration class OmatSivutCacheConfiguration {
    @Bean(name = Array("omatSivutApplicationSystemCacheBuilder"))
    def applicationSystemCacheBuilder(@Value("${application.system.cache.refresh:6}") cacheRefreshTimer: Long): CacheBuilder[String, ApplicationSystem] = {
      CacheBuilder.newBuilder.recordStats.maximumWeight(20).refreshAfterWrite(cacheRefreshTimer, TimeUnit.MINUTES).weigher(new Weigher[String, ApplicationSystem] {
        def weigh(key: String, value: ApplicationSystem): Int = {
          if (value.isActive) {
            1
          }
          else {
            2
          }
        }
      })
    }
  }


