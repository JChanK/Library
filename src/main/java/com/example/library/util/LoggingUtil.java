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

    private static final Logger logger = LoggerFactory.getLogger(LoggingUtil.class);
    private static final Logger performanceLogger = LoggerFactory.getLogger("PERFORMANCE_LOGGER");

    @Pointcut("execution(* com.example.library.controller..*(..))")
    public void controllerMethods() {}

    @SuppressWarnings({"checkstyle:Indentation", "checkstyle:VariableDeclarationUsageDistance"})
    @Around("controllerMethods()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        String methodName = joinPoint.getSignature().toShortString();

        // Логируем вход в метод
        logger.debug("Entering method: {} with arguments: {}", methodName, joinPoint.getArgs());
        stopWatch.start();

            // Выполняем метод
            Object result = joinPoint.proceed();
            stopWatch.stop();

            // Логируем успешное выполнение
            logger.debug("Method '{}' executed successfully in {} ms", methodName,
                    stopWatch.getTotalTimeMillis());
            performanceLogger.info("{} | {} ms | SUCCESS", methodName,
                    stopWatch.getTotalTimeMillis());

            return result;
    }
}