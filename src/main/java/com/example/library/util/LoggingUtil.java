package com.example.library.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingUtil {
    private final Logger logger = LoggerFactory.getLogger(LoggingUtil.class);
    private final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE_LOGGER");

    @Pointcut("execution(* com.example.library.controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        logger.info("Method called: {} with args: {}", methodName, joinPoint.getArgs());

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("Method {} returned: {}", methodName, result);
            performanceLogger.info("{} | {} ms", methodName, duration);

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("Exception in method {}: {}", methodName, e.getMessage(), e);
            performanceLogger.info("{} | {} ms (ERROR)", methodName, duration);
            throw e;
        }
    }
}