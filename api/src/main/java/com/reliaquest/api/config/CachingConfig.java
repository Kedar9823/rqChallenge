package com.reliaquest.api.config;

import java.util.Arrays;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CachingConfig {

    public static final String EMP_CACHE = "employeedetails";

    public static final long EMP_CACHE_TTL = 60L * 1000L;

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setAsyncCacheMode(true);
        cacheManager.setCacheNames(Arrays.asList(EMP_CACHE));
        return cacheManager;
    }
}
