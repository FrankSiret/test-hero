package com.test.hero.service;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
@Component
public class RequestTimingAspect {

    private final Logger log = LoggerFactory.getLogger(RequestTimingAspect.class);
  
    @Around("@annotation(RequestTiming)")
    public Object measureRequestTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        log.info("Request Time : {} ms", duration);
        return result;
    }
}


