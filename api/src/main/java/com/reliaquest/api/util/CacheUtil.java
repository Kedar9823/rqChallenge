package com.reliaquest.api.util;

import com.reliaquest.api.config.CachingConfig;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class CacheUtil {

    @Autowired
    private CacheManager cacheManager;

    @AfterReturning("execution(* com.reliaquest.api.service.EmployeeService.createEmployee(..))")
    public void refreshCacheAfterCreate(JoinPoint joinPoint) {
        cacheEvict();
    }

    @AfterReturning("execution(* com.reliaquest.api.service.EmployeeService.deleteEmployee(..))")
    public void refreshCacheAfterDelete(JoinPoint joinPoint) {
        cacheEvict();
    }

    public void cacheEvict() {
        cacheManager.getCache(CachingConfig.EMP_CACHE).clear();
    }
}
