package com.example.library.util;

import com.example.library.exception.BadRequestException;
import com.example.library.exception.LogProcessingException;
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
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws LogProcessingException {
        StopWatch stopWatch = new StopWatch();
        String methodName = joinPoint.getSignature().toShortString();

        // Log method entry (avoid sensitive data in args)
        logger.debug("Entering method: {} with arguments: {}", methodName, joinPoint.getArgs());
        stopWatch.start();

        try {
            // Proceed with method execution
            Object result = joinPoint.proceed();
            stopWatch.stop();

            // Log successful execution
            logger.debug("Method {} executed successfully in {} ms", methodName,
                    stopWatch.getTotalTimeMillis());
            performanceLogger.info("{} | {} ms | SUCCESS", methodName,
                    stopWatch.getTotalTimeMillis());

            return result;

        } catch (ResourceNotFoundException | BadRequestException e) {
            // Stop the timer if running
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }

            // Log the business exception with context
            logger.warn("Business exception in {}: {}", methodName, e.getMessage());
            performanceLogger.warn("{} | {} ms | {}: {}", methodName,
                    stopWatch.getTotalTimeMillis(),
                    e.getClass().getSimpleName(), e.getMessage());

            // Rethrow with additional context
            throw new LogProcessingException("Business exception in method: " + methodName, e);

        } catch (Throwable e) {
            // Stop the timer if running
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }

            // Log unexpected exceptions with stack trace
            logger.error("Unexpected error in {}: {}", methodName, e.getMessage(), e);
            performanceLogger.error("{} | {} ms | FAILED: {}", methodName,
                    stopWatch.getTotalTimeMillis(),
                    e.getClass().getSimpleName());

            // Rethrow with additional context
            throw new LogProcessingException("Unexpected error in method: " + methodName, e);
        }
    }
}