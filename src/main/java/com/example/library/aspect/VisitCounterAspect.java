package com.example.library.aspect;

import com.example.library.annotation.CountVisit;
import com.example.library.service.VisitCounterService;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class VisitCounterAspect {
    private final VisitCounterService visitCounterService;

    public VisitCounterAspect(VisitCounterService visitCounterService) {
        this.visitCounterService = visitCounterService;
    }

    @Around("@annotation(countVisit)")
    public Object countVisit(ProceedingJoinPoint joinPoint, CountVisit countVisit)
            throws Throwable {
        String url = countVisit.value().isEmpty()
                ? resolveUrlFromRequest(joinPoint)
                : countVisit.value();

        visitCounterService.incrementCounter(url);
        return joinPoint.proceed();
    }

    private String resolveUrlFromRequest(ProceedingJoinPoint joinPoint) {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();

        return request.getRequestURI();
    }

    private String resolveUrlFromMapping(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
        RequestMapping classMapping = method.getDeclaringClass()
                .getAnnotation(RequestMapping.class);

        String baseUrl = classMapping != null ? String.join("", classMapping.value()) : "";
        String methodUrl = methodMapping != null ? String.join("", methodMapping.value()) : "";

        return baseUrl + methodUrl;
    }
}