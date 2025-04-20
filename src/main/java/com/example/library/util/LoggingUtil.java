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

        // Логируем вход в метод
        logger.debug("Entering method: {} with arguments: {}", methodName, joinPoint.getArgs());
        stopWatch.start();

        try {
            // Выполняем метод
            Object result = joinPoint.proceed();
            stopWatch.stop();

            // Логируем успешное выполнение
            logger.debug("Method '{}' executed successfully in {} ms", methodName,
                    stopWatch.getTotalTimeMillis());
            performanceLogger.info("{} | {} ms | SUCCESS", methodName,
                    stopWatch.getTotalTimeMillis());

            return result;

        } catch (ResourceNotFoundException | BadRequestException e) {
            // Останавливаем таймер, если он еще работает
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }

            // Логируем бизнес-исключение с полным стектрейсом на DEBUG уровне
            logger.warn("Business exception in method '{}': {}", methodName, e.getMessage());
            logger.debug("Business exception stack trace:", e);
            performanceLogger.warn("{} | {} ms | {}: {}",
                    methodName,
                    stopWatch.getTotalTimeMillis(),
                    e.getClass().getSimpleName(),
                    e.getMessage());

            // Пробрасываем оригинальное исключение, так как оно уже было залогировано
            throw e;

        } catch (Throwable e) {
            // Останавливаем таймер, если он еще работает
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }

            // Логируем неожиданное исключение
            logger.error("Unexpected error in method '{}': {}", methodName, e.getMessage(), e);
            performanceLogger.error("{} | {} ms | FAILED: {}",
                    methodName,
                    stopWatch.getTotalTimeMillis(),
                    e.getClass().getSimpleName());

            // Пробрасываем исключение с контекстной информацией
            throw new LogProcessingException(
                    String.format("Unexpected error occurred in method '%s'. Cause: %s", methodName,
                            e.getMessage()),
                    e
            );
        }
    }
}