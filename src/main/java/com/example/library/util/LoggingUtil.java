package com.example.library.util;

import com.example.library.exception.LogProcessingException;
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

        try {
            logger.info("Method called: {} with args: {}", methodName, joinPoint.getArgs());
            stopWatch.start();

            Object result = joinPoint.proceed();
            stopWatch.stop();

            logger.info("Method {} executed successfully", methodName);
            performanceLogger.info("{} | {} ms", methodName, stopWatch.getTotalTimeMillis());

            return result;
        } catch (Throwable e) {
            stopWatch.stop();
            String errorMessage = String.format("Method %s failed with %s: %s",
                    methodName,
                    e.getClass().getSimpleName(),
                    e.getMessage());

            logger.error(errorMessage, e);
            performanceLogger.error("{} | {} ms | FAILED: {}",
                    methodName,
                    stopWatch.getTotalTimeMillis(),
                    e.getClass().getSimpleName());

            throw new LogProcessingException(errorMessage, e);
        }
    }
}