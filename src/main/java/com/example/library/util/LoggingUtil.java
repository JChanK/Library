package com.example.library.util;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.ResourceNotFoundException;
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
    private static final Logger logger = LoggerFactory.getLogger(LoggingUtil.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE_LOGGER");

    @Pointcut("execution(* com.example.library.controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        String methodName = joinPoint.getSignature().toShortString();

        logger.debug("Method called: {} with args: {}", methodName, joinPoint.getArgs());
        stopWatch.start();

        try {
            Object result = joinPoint.proceed();
            stopWatch.stop();

            logger.debug("Method {} executed successfully in {} ms",
                    methodName, stopWatch.getTotalTimeMillis());
            performanceLogger.info("{} | {} ms | SUCCESS",
                    methodName, stopWatch.getTotalTimeMillis());
            return result;
        } catch (ResourceNotFoundException | BadRequestException e) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logger.debug("Business exception in {}: {}", methodName, e.getMessage());
            performanceLogger.warn("{} | {} ms | {}: {}",
                    methodName, stopWatch.getTotalTimeMillis(),
                    e.getClass().getSimpleName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            logger.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            performanceLogger.error("{} | {} ms | FAILED: {}",
                    methodName, stopWatch.getTotalTimeMillis(),
                    e.getClass().getSimpleName());
            throw e;
        }
    }
}