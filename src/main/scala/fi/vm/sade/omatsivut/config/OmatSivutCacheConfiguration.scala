package fi.vm.sade.omatsivut.config

import java.util.concurrent.TimeUnit

import com.google.common.cache.CacheBuilder
import fi.vm.sade.haku.oppija.lomake.domain.ApplicationSystem
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.{Bean, Configuration}

@Configuration class OmatSivutCacheConfiguration {
    @Bean(name = Array("omatSivutApplicationSystemCacheBuilder"))
    def applicationSystemCacheBuilder(@Value("${application.system.cache.refresh:6}") cacheRefreshTimer: Long): CacheBuilder[String, ApplicationSystem] =
      CacheBuilder
        .newBuilder
        .recordStats
        .maximumSize(32)
        .refreshAfterWrite(cacheRefreshTimer, TimeUnit.MINUTES)
        .asInstanceOf[CacheBuilder[String, ApplicationSystem]]
  }


