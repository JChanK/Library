package com.example.library.util;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class LoggingUtil {
    private final Logger logger = LoggerFactory.getLogger(LoggingUtil.class);
    private final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE_LOGGER");

    @Pointcut("execution(* com.example.library.controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        String methodName = joinPoint.getSignature().toShortString();

        logger.info("Method called: {} with args: {}", methodName, joinPoint.getArgs());
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();

            logger.info("Method {} returned: {}", methodName, result);
            performanceLogger.info("{} | {} ms", methodName, stopWatch.getTotalTimeMillis());

            return result;
        } catch (Exception e) {
            stopWatch.stop();
            logger.error("Exception in method {}: {}", methodName, e.getMessage(), e);
            performanceLogger.error("{} | {} ms (ERROR) - {}", methodName,
                    stopWatch.getTotalTimeMillis(), e.getMessage());
            throw new RuntimeException("Error in " + methodName + ": " + e.getMessage(), e);
        }
    }
}