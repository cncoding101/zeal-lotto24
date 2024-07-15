package org.example.demoservice.config

import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CacheConfiguration {

    @Bean
    fun cacheManager(): CacheManager {
        return ConcurrentMapCacheManager("customers")
    }
}
